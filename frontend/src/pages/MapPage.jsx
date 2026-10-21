import { useEffect, useState } from "react";
import { api } from "../api/client";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import { getColor } from "../utils/color";
import "leaflet/dist/leaflet.css";
import L from "leaflet";

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl:
    "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
});

export default function MapPage() {
  const [locations, setLocations] = useState([]);

  useEffect(() => {
    api("/api/locations/latest")
      .then(setLocations)
      .catch(() => {});
  }, []);

  return (
    <div>
      <h1>Карта точек мониторинга</h1>
      <MapContainer
        center={[54.3142, 48.4031]}
        zoom={11}
        style={{ height: "70vh", width: "100%", borderRadius: 8 }}
      >
        <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
        {locations.map((l) => (
          <Marker key={l.id} position={[l.latitude, l.longitude]}>
            <Popup>
              <b>{l.name}</b>
              {l.measurements?.map((m) => (
                <div
                  key={m.id}
                  style={{ color: getColor(m.value, m.threshold) }}
                >
                  {m.parameter?.code}: {m.value} {m.parameter?.unit}
                </div>
              ))}
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
}
