package com.strux.document_service.service;

import com.strux.document_service.dto.*;
import com.strux.document_service.enums.DocumentStatus;
import com.strux.document_service.model.Folder;
import com.strux.document_service.repository.FolderRepository;
import com.strux.document_service.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FolderService {

    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;

    @Transactional
    public FolderDto createFolder(FolderCreateRequest request, String createdBy) {
        // Aynı isimde folder var mı kontrol et
        if (folderRepository.existsByNameAndParentFolderIdAndCompanyIdAndIsDeletedFalse(
                request.getName(), request.getParentFolderId(), request.getCompanyId())) {
            throw new RuntimeException("Folder with this name already exists in this location");
        }

        // Parent folder'ı kontrol et ve path oluştur
        String folderPath;
        Integer level;

        if (request.getParentFolderId() != null) {
            Folder parentFolder = folderRepository.findByIdAndIsDeletedFalse(request.getParentFolderId())
                    .orElseThrow(() -> new RuntimeException("Parent folder not found"));

            folderPath = parentFolder.getFolderPath() + "/" + sanitizeFolderName(request.getName());
            level = parentFolder.getLevel() + 1;
        } else {
            folderPath = "/" + sanitizeFolderName(request.getName());
            level = 0;
        }

        Folder folder = Folder.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parentFolderId(request.getParentFolderId())
                .companyId(request.getCompanyId())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .createdBy(createdBy)
                .folderPath(folderPath)
                .level(level)
                .isDeleted(false)
                .build();

        folder = folderRepository.save(folder);
        log.info("Folder created: {} at path: {}", folder.getId(), folderPath);

        return toDto(folder);
    }

    public FolderDto getFolder(String folderId) {
        Folder folder = folderRepository.findByIdAndIsDeletedFalse(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        return toDto(folder);
    }

    public List<FolderDto> getRootFolders(String companyId) {
        return folderRepository.findByCompanyIdAndParentFolderIdIsNullAndIsDeletedFalse(companyId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<FolderDto> getSubFolders(String parentFolderId) {
        return folderRepository.findByParentFolderIdAndIsDeletedFalse(parentFolderId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public FolderDto getFolderTree(String folderId) {
        Folder folder = folderRepository.findByIdAndIsDeletedFalse(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        FolderDto dto = toDto(folder);

        // Alt klasörleri recursive olarak ekle
        List<Folder> subFolders = folderRepository.findByParentFolderIdAndIsDeletedFalse(folderId);
        dto.setSubFolders(subFolders.stream()
                .map(f -> getFolderTree(f.getId()))
                .collect(Collectors.toList()));

        return dto;
    }

    public List<FolderDto> getCompanyFolderTree(String companyId) {
        List<Folder> rootFolders = folderRepository
                .findByCompanyIdAndParentFolderIdIsNullAndIsDeletedFalse(companyId);

        return rootFolders.stream()
                .map(f -> getFolderTree(f.getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public FolderDto updateFolder(String folderId, FolderCreateRequest request) {
        Folder folder = folderRepository.findByIdAndIsDeletedFalse(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        if (request.getName() != null) {
            folder.setName(request.getName());

            // Path'i güncelle
            if (folder.getParentFolderId() != null) {
                Folder parent = folderRepository.findById(folder.getParentFolderId())
                        .orElseThrow(() -> new RuntimeException("Parent folder not found"));
                folder.setFolderPath(parent.getFolderPath() + "/" + sanitizeFolderName(request.getName()));
            } else {
                folder.setFolderPath("/" + sanitizeFolderName(request.getName()));
            }
        }

        if (request.getDescription() != null) {
            folder.setDescription(request.getDescription());
        }

        folder = folderRepository.save(folder);
        return toDto(folder);
    }

    @Transactional
    public void deleteFolder(String folderId, boolean deleteDocuments) {
        Folder folder = folderRepository.findByIdAndIsDeletedFalse(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        // Alt klasörleri kontrol et
        List<Folder> subFolders = folderRepository.findByParentFolderIdAndIsDeletedFalse(folderId);
        if (!subFolders.isEmpty()) {
            throw new RuntimeException("Cannot delete folder with subfolders. Delete subfolders first.");
        }

        // Dokümanlara ne yapılacak?
        if (deleteDocuments) {
            // Tüm dokümanları sil
            List<com.strux.document_service.model.Document> documents =
                    documentRepository.findByFolderIdAndStatus(folderId, DocumentStatus.ACTIVE);
            documents.forEach(doc -> doc.setStatus(DocumentStatus.DELETED));
            documentRepository.saveAll(documents);
        } else {
            // Dokümanları root'a taşı
            List<com.strux.document_service.model.Document> documents =
                    documentRepository.findByFolderIdAndStatus(folderId, DocumentStatus.ACTIVE);
            documents.forEach(doc -> doc.setFolderId(null));
            documentRepository.saveAll(documents);
        }

        folder.setIsDeleted(true);
        folderRepository.save(folder);

        log.info("Folder deleted: {}", folderId);
    }

    @Transactional
    public void moveFolder(String folderId, String newParentFolderId) {
        Folder folder = folderRepository.findByIdAndIsDeletedFalse(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        // Kendini kendi içine taşımaya çalışıyor mu?
        if (folderId.equals(newParentFolderId)) {
            throw new RuntimeException("Cannot move folder into itself");
        }

        // Circular dependency kontrolü
        if (newParentFolderId != null && isDescendant(newParentFolderId, folderId)) {
            throw new RuntimeException("Cannot move folder into its own descendant");
        }

        String newPath;
        Integer newLevel;

        if (newParentFolderId != null) {
            Folder newParent = folderRepository.findByIdAndIsDeletedFalse(newParentFolderId)
                    .orElseThrow(() -> new RuntimeException("New parent folder not found"));
            newPath = newParent.getFolderPath() + "/" + sanitizeFolderName(folder.getName());
            newLevel = newParent.getLevel() + 1;
        } else {
            newPath = "/" + sanitizeFolderName(folder.getName());
            newLevel = 0;
        }

        folder.setParentFolderId(newParentFolderId);
        folder.setFolderPath(newPath);
        folder.setLevel(newLevel);

        folderRepository.save(folder);

        // Alt klasörlerin path'lerini güncelle (recursive)
        updateSubFolderPaths(folderId);
    }

    private void updateSubFolderPaths(String parentFolderId) {
        List<Folder> subFolders = folderRepository.findByParentFolderIdAndIsDeletedFalse(parentFolderId);

        Folder parent = folderRepository.findById(parentFolderId)
                .orElseThrow(() -> new RuntimeException("Parent folder not found"));

        for (Folder subFolder : subFolders) {
            String newPath = parent.getFolderPath() + "/" + sanitizeFolderName(subFolder.getName());
            subFolder.setFolderPath(newPath);
            subFolder.setLevel(parent.getLevel() + 1);
            folderRepository.save(subFolder);

            // Recursive olarak alt klasörleri güncelle
            updateSubFolderPaths(subFolder.getId());
        }
    }

    private boolean isDescendant(String potentialDescendantId, String ancestorId) {
        Folder current = folderRepository.findById(potentialDescendantId).orElse(null);

        while (current != null && current.getParentFolderId() != null) {
            if (current.getParentFolderId().equals(ancestorId)) {
                return true;
            }
            current = folderRepository.findById(current.getParentFolderId()).orElse(null);
        }

        return false;
    }

    private String sanitizeFolderName(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private FolderDto toDto(Folder folder) {
        Long documentCount = documentRepository.countByFolderId(folder.getId());

        return FolderDto.builder()
                .id(folder.getId())
                .name(folder.getName())
                .description(folder.getDescription())
                .parentFolderId(folder.getParentFolderId())
                .companyId(folder.getCompanyId())
                .entityType(folder.getEntityType())
                .entityId(folder.getEntityId())
                .createdBy(folder.getCreatedBy())
                .createdAt(folder.getCreatedAt())
                .folderPath(folder.getFolderPath())
                .level(folder.getLevel())
                .documentCount(documentCount.intValue())
                .build();
    }
}