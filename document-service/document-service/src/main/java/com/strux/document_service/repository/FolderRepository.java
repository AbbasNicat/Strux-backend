package com.strux.document_service.repository;

import com.strux.document_service.enums.EntityType;
import com.strux.document_service.model.Folder;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface FolderRepository extends MongoRepository<Folder, String> {

    List<Folder> findByCompanyIdAndIsDeletedFalse(String companyId);

    List<Folder> findByParentFolderIdAndIsDeletedFalse(String parentFolderId);

    List<Folder> findByCompanyIdAndParentFolderIdIsNullAndIsDeletedFalse(String companyId);

    Optional<Folder> findByIdAndIsDeletedFalse(String id);

    List<Folder> findByEntityTypeAndEntityIdAndIsDeletedFalse(EntityType entityType, String entityId);

    boolean existsByNameAndParentFolderIdAndCompanyIdAndIsDeletedFalse(
            String name, String parentFolderId, String companyId
    );
}
