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
    response = client.get("/api/v1/sources/jellyfin/items")
    assert response.status_code == 503


def test_search_maps_movie_and_tv_results(monkeypatch):
    from app.routers import catalog

    async def fake_tmdb(path, params=None):
        assert path == "search/multi"
        return {"results": [
            {"id": 1, "title": "A Movie", "media_type": "movie", "release_date": "2024-01-01"},
            {"id": 2, "name": "A Series", "media_type": "tv", "first_air_date": "2023-01-01"},
            {"id": 3, "name": "An Actor", "media_type": "person"},
        ]}

    monkeypatch.setattr(catalog, "_tmdb_get", fake_tmdb)
    response = client.get("/api/v1/catalog/search?q=example&media_type=multi")
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 2
    assert {item["media_type"] for item in data} == {"movie", "tv"}
    assert {item["title"] for item in data} == {"A Movie", "A Series"}


def test_trending_tv_uses_tv_endpoint(monkeypatch):
    from app.routers import catalog

    async def fake_tmdb(path, params=None):
        assert path == "trending/tv/week"
        return {"results": [{"id": 22, "name": "Series Name", "first_air_date": "2022-05-01"}]}

    monkeypatch.setattr(catalog, "_tmdb_get", fake_tmdb)
    response = client.get("/api/v1/catalog/trending?media_type=tv")
    assert response.status_code == 200
    assert response.json()[0]["media_type"] == "tv"
    assert response.json()[0]["title"] == "Series Name"


def test_invalid_media_type_is_rejected():
    response = client.get("/api/v1/catalog/popular/person")
    assert response.status_code == 400
