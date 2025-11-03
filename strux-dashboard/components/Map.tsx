'use client'

import { useEffect, useRef } from 'react'

export default function Map() {
  const mapRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (typeof window !== 'undefined' && mapRef.current) {
      // Dynamically import leaflet
      import('leaflet').then((L) => {
        // Clear existing map if any
        if (!mapRef.current) return

        mapRef.current.innerHTML = ''

        // Initialize map centered on Azerbaijan
        const map = L.map(mapRef.current).setView([40.4093, 49.8671], 7)

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        }).addTo(map)

        // Add markers for example projects
        const projects = [
          { lat: 40.4093, lng: 49.8671, name: 'Baku Project' },
          { lat: 39.8265, lng: 46.7656, name: 'Shusha Reconstruction' },
          { lat: 40.3777, lng: 49.8920, name: 'Khirdalan Development' },
        ]

        projects.forEach((project) => {
          L.marker([project.lat, project.lng])
            .addTo(map)
            .bindPopup(`<b>${project.name}</b>`)
        })
      })
    }
  }, [])

  return <div ref={mapRef} className="w-full h-full min-h-[300px] rounded-lg" />
}
