# Vozni Park - Fleet Management System

Sveobuhvatna aplikacija za upravljanje voznim parkom izgrađena sa Spring Boot frameworkom. Sistem omogućava kompletno upravljanje vozilima, vozačima, putnim nalozima i svim povezanim operacijama kroz REST API interfejs.

## 📋 Osnovne Informacije

- **Naziv projekta:** Vozni Park
- **Verzija:** 1.0.0
- **Java verzija:** 21
- **Spring Boot verzija:** 4.0.2
- **Baza podataka:** MySQL 8.0
- **Port:** 8080
- **Build alat:** Maven

## 🎯 Funkcionalnosti

### Glavni Entiteti
- **Vozila (Vehicles)** - Upravljanje voznim parkom
- **Vozači (Drivers)** - Evidencija vozača i njihovih dozvola
- **Putni Nalozi (Travel Orders)** - Kreiranje i praćenje putnih naloga
- **Korisnici (App Users)** - Upravljanje korisnicima sistema

### Pomoćni Entiteti
- Brendovi vozila (Brands)
- Modeli vozila (Vehicle Models)
- Tipovi goriva (Fuel Types)
- Lokacijske jedinice (Location Units)
- Registracije (Registrations)
- Vozačke dozvole (Drivers Licenses)
- Prvo-pomoćni paketi (First Aid Kits)
- Uloge korisnika (Roles)

## 🏗️ Arhitektura Projekta
```
vozni-park/
├── src/main/java/com/example/vozni_park/
│   ├── controller/      # REST Controllers (14 kontrolera)
│   ├── service/         # Business logic layer
│   ├── repository/      # JPA Repositories
│   ├── entity/          # JPA Entities
│   ├── dto/             # Data Transfer Objects
│   │   ├── request/     # Request DTOs
│   │   ├── response/    # Response DTOs
│   │   └── summary/     # Summary DTOs
│   ├── mapper/          # Entity-DTO Mappers
│   └── config/          # Configuration classes
└── src/main/resources/
    ├── db/changelog/    # Liquibase migrations
    └── application.properties
```

## 🚀 Pokretanje Projekta

### Preduslov
- Java 21 ili novija verzija
- MySQL 8.0
- Maven 3.6+

### Konfiguracija Baze Podataka

1. Kreirajte MySQL bazu podataka:
```sql
CREATE DATABASE vozni_park CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Ažurirajte `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3307/vozni_park
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Pokretanje Aplikacije
```bash
# Kloniranje repozitorijuma
git clone <repository-url>
cd vozni-park

# Build projekta
mvn clean install

# Pokretanje aplikacije
mvn spring-boot:run
```

Aplikacija će biti dostupna na: `http://localhost:8080`

## 📚 API Dokumentacija

### Swagger UI
Nakon pokretanja aplikacije, Swagger dokumentacija je dostupna na:
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **OpenAPI YAML:** http://localhost:8080/v3/api-docs.yaml

### Glavni Endpoint-i

#### Vozila (Vehicles)
- `GET /api/vehicles` - Lista svih vozila
- `GET /api/vehicles/{id}` - Vozilo po ID-u
- `POST /api/vehicles` - Kreiranje novog vozila
- `PUT /api/vehicles/{id}` - Ažuriranje vozila
- `DELETE /api/vehicles/{id}` - Brisanje vozila
- `GET /api/vehicles/sap/{sapNumber}` - Pretraga po SAP broju
- `GET /api/vehicles/status/{status}` - Filter po statusu
- `GET /api/vehicles/location/{locationId}` - Vozila po lokaciji

#### Vozači (Drivers)
- `GET /api/drivers` - Lista svih vozača
- `GET /api/drivers/{id}` - Vozač po ID-u
- `POST /api/drivers` - Kreiranje novog vozača
- `PUT /api/drivers/{id}` - Ažuriranje vozača
- `DELETE /api/drivers/{id}` - Brisanje vozača
- `GET /api/drivers/available` - Dostupni vozači
- `GET /api/drivers/search?name={name}` - Pretraga po imenu

#### Putni Nalozi (Travel Orders)
- `GET /api/travel-orders` - Lista svih putnih naloga
- `GET /api/travel-orders/{id}` - Putni nalog po ID-u
- `POST /api/travel-orders` - Kreiranje novog naloga
- `PUT /api/travel-orders/{id}` - Ažuriranje naloga
- `PATCH /api/travel-orders/{id}/complete` - Zatvaranje naloga
- `GET /api/travel-orders/status/{status}` - Filter po statusu

#### Korisnici (Users)
- `GET /api/users` - Lista svih korisnika
- `GET /api/users/{id}` - Korisnik po ID-u
- `POST /api/users` - Kreiranje novog korisnika
- `PUT /api/users/{id}` - Ažuriranje korisnika
- `POST /api/users/login` - Login korisnika

## 🗄️ Baza Podataka

### Liquibase Migracije
Projekat koristi Liquibase za upravljanje šemom baze podataka. Sve migracije se nalaze u:
```
src/main/resources/db/changelog/
```

Pri prvom pokretanju, Liquibase automatski:
1. Kreira sve potrebne tabele (20 tabela)
2. Postavlja foreign key constraint-e
3. Kreira indekse za optimizaciju
4. Inicijalizuje osnovne podatke (opciono)
5. 

## 🔧 Tehnologije

- **Backend Framework:** Spring Boot 4.0.2
- **ORM:** Spring Data JPA / Hibernate
- **Database Migration:** Liquibase
- **Database:** MySQL 8.0
- **API Documentation:** SpringDoc OpenAPI 3 (Swagger)
- **Validation:** Jakarta Validation
- **Build Tool:** Maven
- **Java Version:** 21

### Glavne Zavisnosti
```xml
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- liquibase-core
- mysql-connector-j
- lombok
- springdoc-openapi-starter-webmvc-ui
- jakarta.validation-api
```

## 📦 DTO Pattern

Projekat implementira DTO (Data Transfer Object) pattern za čist API:
- **Request DTOs** - Za POST/PUT operacije sa validacijom
- **Response DTOs** - Za GET operacije bez lazy loading problema
- **Summary DTOs** - Za nested objekte (sprečavanje circular references)
- **Mappers** - Konverzija između Entity i DTO objekata

### Prednosti DTO Implementacije
✅ Nema lazy loading exceptions  
✅ Nema circular reference problema   
✅ Automatska validacija input podataka

## 🧪 Testiranje

API endpoint-i mogu se testirati preko:
- **Swagger UI** - Interaktivno testiranje na http://localhost:8080/swagger-ui.html


## 📝 Buduća Poboljšanja

- [ ] Spring Security implementacija
- [ ] JWT autentifikacija
- [ ] Role-based access control (RBAC)
- [ ] Unit i Integration testovi
- [ ] Docker kontejnerizacija
- [ ] CI/CD pipeline
- [ ] Fronted aplikacija (React/Angular)
- [ ] Real-time notifikacije
- [ ] Email servisi

## 👨‍💻 Autor

**Vaše Ime**
- Nemanja Vukelić 2025/3827
- Email: vas.email@example.com
