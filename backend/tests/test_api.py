from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"


def test_config_status_never_returns_secrets():
    response = client.get("/api/v1/config/status")
    assert response.status_code == 200
    data = response.json()
    assert data["secrets_exposed"] is False
    assert "tmdb_api_key" not in data
    assert "jellyfin_api_key" not in data


def test_tmdb_search_requires_configuration(monkeypatch):
    from app.core.config import get_settings

    monkeypatch.setattr(get_settings(), "tmdb_api_key", "")
    response = client.get("/api/v1/catalog/search?q=Inception")
    assert response.status_code == 503


def test_jellyfin_requires_configuration(monkeypatch):
    from app.core.config import get_settings

    settings = get_settings()
    monkeypatch.setattr(settings, "jellyfin_base_url", "")
    monkeypatch.setattr(settings, "jellyfin_api_key", "")
    response = client.get("/api/v1/sources/jellyfin/movies")
    assert response.status_code == 503
