from abc import ABC, abstractmethod
from dataclasses import dataclass
from enum import Enum


class ProviderStatus(str, Enum):
    AVAILABLE = "available"
    UNAVAILABLE = "unavailable"
    NOT_CONFIGURED = "not_configured"


@dataclass
class ProviderHealth:
    provider_id: str
    status: ProviderStatus
    message: str = ""


class MovieProvider(ABC):
    """Contract for documented and authorized provider integrations."""

    provider_id: str
    display_name: str

    @abstractmethod
    async def health(self) -> ProviderHealth:
        raise NotImplementedError

    @abstractmethod
    async def search(self, title: str) -> list[dict]:
        raise NotImplementedError
