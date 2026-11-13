# ZYLO privacy information

ZYLO is an independently maintained media client. The following describes the account and service integrations in the current source, not a promise about third-party services.

- Account sign-in, profile data and synchronized watch progress use the configured account service. The default build connects to the upstream account service at api.nuvio.tv. Its [privacy policy](https://nuvio.tv/privacy-policy) applies to that service. Custom deployments may use a different account service.
- The browser stores local settings and session information to keep the client usable across visits. Signing out and clearing browser site data are separate actions.
- Optional integrations (including metadata, subtitles, account linking and user-selected media providers) communicate with their own services. Their privacy policies and terms apply; requests may reveal information such as IP address and the requested title.
- QR login is generated on-device from the account service's approval URL. Do not share login codes or account tokens.

For questions about the ZYLO client or to report a privacy issue, contact the maintainer through the [project repository](https://github.com/anshdeepofficial1/ZYLO/issues). Do not include private account details in a public issue. Requests concerning account data held by an external service should be directed to that service.

This notice should be reviewed by the maintainer before distribution, especially if the deployed account service, analytics or storage configuration changes.
