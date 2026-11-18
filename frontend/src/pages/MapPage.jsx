import { useEffect, useRef, useState } from "react";
import { api } from "../api/client";
import { getColor } from "../utils/color";
import "ol/ol.css";
import Map from "ol/Map";
import View from "ol/View";
import TileLayer from "ol/layer/Tile";
import OSM from "ol/source/OSM";
import VectorLayer from "ol/layer/Vector";
import VectorSource from "ol/source/Vector";
import Feature from "ol/Feature";
import Point from "ol/geom/Point";
import { fromLonLat } from "ol/proj";
import { Style, Circle, Fill, Stroke } from "ol/style";
import Overlay from "ol/Overlay";

export default function MapPage() {
  const mapRef = useRef(null);
  const mapContainerRef = useRef(null);
  const [locations, setLocations] = useState([]);

  useEffect(() => {
    api("/api/locations/latest")
      .then(setLocations)
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (!mapContainerRef.current || mapRef.current) return;

    // 1. Инициализация карты
    mapRef.current = new Map({
      target: mapContainerRef.current,
      layers: [
        new TileLayer({
          source: new OSM(), // Бесплатные тайлы OSM (для РФ автоматически на русском)
        }),
      ],
      view: new View({
        center: fromLonLat([48.4031, 54.3142]), // Ульяновск [lon, lat]
        zoom: 11,
      }),
    });

    // 2. Создаем элемент для попапа
    const popupEl = document.getElementById("popup");
    const overlay = new Overlay({
      element: popupEl,
      positioning: "bottom-center",
      stopEvent: false,
      offset: [0, -10],
    });
    mapRef.current.addOverlay(overlay);

    // Очистка при размонтировании
    return () => {
      mapRef.current.setTarget(undefined);
      mapRef.current = null;
    };
  }, []);

  useEffect(() => {
    if (!mapRef.current || locations.length === 0) return;

    // Удаляем старый слой с маркерами, если он есть
    const layers = mapRef.current.getLayers().getArray();
    const vectorLayerIndex = layers.findIndex(
      (l) => l.get("name") === "markers",
    );
    if (vectorLayerIndex !== -1) {
      mapRef.current.getLayers().removeAt(vectorLayerIndex);
    }

    // 3. Создаем векторные объекты (маркеры)
    const features = locations.map((l) => {
      const maxRatio =
        l.measurements?.reduce((max, m) => {
          const ratio = m.threshold ? m.value / m.threshold : 0;
          return ratio > max ? ratio : max;
        }, 0) || 0;

      const color =
        maxRatio >= 1.0 ? "#ff4d4f" : maxRatio >= 0.8 ? "#faad14" : "#52c41a";

      const feature = new Feature({
        geometry: new Point(fromLonLat([l.longitude, l.latitude])),
        name: l.name,
        measurements: l.measurements,
      });

      feature.setStyle(
        new Style({
          image: new Circle({
            radius: 8,
            fill: new Fill({ color }),
            stroke: new Stroke({ color: "#fff", width: 2 }),
          }),
        }),
      );

      return feature;
    });

    const vectorSource = new VectorSource({ features });
    const vectorLayer = new VectorLayer({ source: vectorSource });
    vectorLayer.set("name", "markers");
    mapRef.current.addLayer(vectorLayer);

    // 4. Обработчик клика по маркеру
    mapRef.current.on("click", (evt) => {
      const feature = mapRef.current.forEachFeatureAtPixel(evt.pixel, (f) => f);
      const popupEl = document.getElementById("popup");
      const contentEl = document.getElementById("popup-content");

      if (feature && popupEl && contentEl) {
        const coords = feature.getGeometry().getCoordinates();
        mapRef.current.getOverlays().getArray()[0].setPosition(coords);

        const measurementsHtml =
          feature
            .get("measurements")
            ?.map(
              (
                m,
              ) => `<div style="margin: 4px 0; padding: 4px; background: ${getColor(m.value, m.threshold)}20; border-radius: 4px;">
              <span style="font-weight: bold; color: ${getColor(m.value, m.threshold)};">
                ${m.parameter?.code}: ${m.value} ${m.parameter?.unit}
              </span><br/>
              <span style="font-size: 11px; color: #666;">Порог: ${m.threshold}</span>
            </div>`,
            )
            .join("") || "Нет данных";

        contentEl.innerHTML = `<b style="font-size: 14px; display: block; margin-bottom: 8px;">${feature.get("name")}</b>${measurementsHtml}`;
        popupEl.style.display = "block";
      } else {
        if (popupEl) popupEl.style.display = "none";
        mapRef.current.getOverlays().getArray()[0].setPosition(undefined);
      }
    });
  }, [locations]);

  return (
    <div>
      <h1>Карта точек мониторинга</h1>
      <div
        ref={mapContainerRef}
        style={{
          height: "70vh",
          width: "100%",
          borderRadius: 8,
          position: "relative",
        }}
      />
      {/* Скрытый элемент для попапа, которым управляет OpenLayers */}
      <div
        id="popup"
        style={{
          display: "none",
          position: "absolute",
          backgroundColor: "white",
          padding: "12px",
          borderRadius: "6px",
          border: "1px solid #ccc",
          boxShadow: "0 4px 12px rgba(0,0,0,0.15)",
          fontFamily: "Arial, sans-serif",
          minWidth: "160px",
          zIndex: 1000,
        }}
      >
        <div id="popup-content" />
      </div>
    </div>
  );
}
