📜 Certificate Chain Generator

This project is a Spring Boot web application that generates a PKCS#12 keystore (.p12) containing a full certificate chain including:

    Root Certificate

    Intermediate Certificate

    Endpoint Certificate

    Owner Certificate

The generated keystore is made available for download via a simple HTTP GET endpoint.
🔧 Features

    Generates RSA key pairs for each entity in the certificate chain

    Creates X.509 certificates signed in a hierarchical manner (Root → Intermediate → Endpoint → Owner)

    Builds a .p12 keystore including the entire chain

    Returns the keystore as a downloadable file via HTTP response

🚀 Getting Started
Prerequisites

    Java 17+

    Maven

    Internet connection (for Maven dependencies)

Dependencies

    Spring Boot (Web)

    Bouncy Castle for certificate generation and management

Running the Project

mvn clean install
mvn spring-boot:run

Once the application is running, visit:

http://localhost:8080/createKeyStore

A keystore.p12 file will be generated and downloaded automatically.
🧩 Endpoint
GET /createKeyStore

Response:

    Content-Type: application/octet-stream

    Download: keystore.p12

🔐 Keystore Details

    Type: PKCS#12

    Alias: mykey

    Password: password

    Certificate Chain Order:
    Owner → Endpoint → Intermediate → Root

📁 Project Structure

src/
└── main/
└── java/
└── com/
└── keystore/
└── controller/
└── CCController.java

⚠️ Notes

    This example uses SHA256withRSA as the signature algorithm.

    The certificates are valid for 1 year from the date of generation.

    Bouncy Castle's X509V3CertificateGenerator is deprecated and may be replaced with JcaX509v3CertificateBuilder in future improvements.

📜 License

This project is provided for educational and demonstration purposes. Please ensure compliance with security policies and standards before deploying in production environments.

Would you like a version of this README with markdown badges or code snippets for Dockerization or REST clients (e.g., Postman)?
