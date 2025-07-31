## Mocking Bulk Payment With Spring Boot Batch

### Overview

This project showcases a bulk payment system built with Spring Boot. It leverages Spring Batch for processing, Quartz
for scheduling, RabbitMQ for messaging, and PostgreSQL as the database.

The system exposes a REST API that allows users to upload a CSV file containing payment details to initiate mock bulk
payments. Confirmed payments are persisted in the database, and a scheduled job periodically processes these payments in
batches. After processing, the system updates the status and related information for each payment record.

How to show passwords in PostgreSQL:
```sql
SELECT usename, passwd FROM pg_shadow WHERE passwd IS NOT NULL;
```

### Technologies Used

- **SB Webflux**: For building reactive web applications.
- **Spring Batch**: For batch processing of payment records.
- **Quartz**: For scheduling batch jobs.
- **RabbitMQ**: For messaging and communication between downstream and upstream.
- **PostgreSQL**: For data persistence.
- **Redis**: For caching and temporary data storage.
- **Docker**: For containerization of the application.
- **Grafana Stack**: For monitoring and visualization (Grafana, Prometheus).
- **ELK Stack**: For logging and monitoring (Elasticsearch, Logstash, Kibana).
