## Spring Boot Reactive Redis Batch Elasticsearch

This project demonstrates how to use Spring Boot with Reactive Redis and Elasticsearch to perform batch operations. It
includes a simple REST API to interact with the data.

```shell
curl --location 'localhost:10000/api/v1/batches/upload' \
--form 'file=@"test.xlsx"' \
--form 'batchOwnerName="Testing"'
```

To confirm a batch, you can use the following endpoint:

```http request
POST /api/v1/batches/{ID}/confirm
```

This will confirm the batch with ID, from staging to production table.

To migrate data from PostgreSQL to Elasticsearch, you can use the provided migration endpoint.

```http request
GET /api/v1/batches/migration
```

To retrieve all batch records, you can use the following endpoint:

```http request
GET /api/v1/batches/{ID}/records
```

To search for customer codes using a fuzzy search, you can use the following endpoint:

```http request
POST /api/v1/batches/fuzzy-search?searchTerm=search
```