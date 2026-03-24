# Traffic Light Controller API Documentation

## Overview

The Traffic Light Controller API is an enterprise-grade REST API for managing traffic intersections, traffic lights, and state transitions with real-time event processing using Kafka.

**Base URL**: `http://localhost:9900`

**API Version**: 1.0.0

## Authentication

Currently, the API does not require authentication. Future versions will implement OAuth2/JWT authentication.

## Common Headers

All requests should include the following headers:

```
Content-Type: application/json
Accept: application/json
X-Correlation-ID: <optional-correlation-id>
```

The `X-Correlation-ID` header is optional but recommended for request tracing. If not provided, the system will generate one automatically.

## API Endpoints

### Intersections API

#### Create Intersection
```http
POST /api/intersections
Content-Type: application/json

{
  "name": "Main St & 1st Ave",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "status": "ACTIVE"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "message": "Intersection created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Main St & 1st Ave",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "status": "ACTIVE",
    "isActive": true,
    "createdAt": "2024-01-27T10:30:00.000Z",
    "updatedAt": "2024-01-27T10:30:00.000Z"
  },
  "timestamp": "2024-01-27T10:30:00.000Z",
  "correlationId": "abc123"
}
```

#### Get Intersection by ID
```http
GET /api/intersections/{id}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Intersection retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Main St & 1st Ave",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "status": "ACTIVE",
    "isActive": true,
    "trafficLights": [
      {
        "id": "660e8400-e29b-41d4-a716-446655440001",
        "direction": "NORTH",
        "currentState": "RED"
      }
    ]
  }
}
```

#### Update Intersection
```http
PUT /api/intersections/{id}
Content-Type: application/json

{
  "name": "Main St & 1st Ave (Updated)",
  "status": "MAINTENANCE"
}
```

#### Delete Intersection
```http
DELETE /api/intersections/{id}
```

#### List All Intersections
```http
GET /api/intersections?page=0&size=20&sort=name,asc
```

#### Pause Intersection
```http
POST /api/intersections/{id}/pause
```

#### Resume Intersection
```http
POST /api/intersections/{id}/resume
```

#### Get Intersection Status
```http
GET /api/intersections/{id}/status
```

#### Get Intersection History
```http
GET /api/intersections/{id}/history?page=0&size=50
```

#### Change Intersection Lights
```http
POST /api/intersections/{id}/lights/change
Content-Type: application/json

{
  "direction": "NORTH",
  "newState": "GREEN",
  "reason": "Manual override"
}
```

### Traffic Lights API

#### Create Traffic Light
```http
POST /api/traffic-lights
Content-Type: application/json

{
  "intersectionId": "550e8400-e29b-41d4-a716-446655440000",
  "direction": "NORTH",
  "currentState": "RED"
}
```

#### Get Traffic Light by ID
```http
GET /api/traffic-lights/{id}
```

#### Update Traffic Light State
```http
PUT /api/traffic-lights/{id}/state
Content-Type: application/json

{
  "newState": "GREEN",
  "reason": "Scheduled change"
}
```

#### Get Traffic Light History
```http
GET /api/traffic-lights/{id}/history?page=0&size=50
```

### Light Sequences API

#### Create Light Sequence
```http
POST /api/sequences
Content-Type: application/json

{
  "intersectionId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Standard Sequence",
  "steps": [
    {
      "direction": "NORTH",
      "state": "GREEN",
      "durationSeconds": 30
    },
    {
      "direction": "NORTH",
      "state": "YELLOW",
      "durationSeconds": 5
    },
    {
      "direction": "NORTH",
      "state": "RED",
      "durationSeconds": 30
    }
  ]
}
```

#### Activate Sequence
```http
POST /api/sequences/{id}/activate
```

#### Deactivate Sequence
```http
POST /api/sequences/{id}/deactivate
```

### System Health API

#### Get System Health
```http
GET /api/health
```

**Response** (200 OK):
```json
{
  "status": "UP",
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "SELECT 1"
      }
    },
    "kafka": {
      "status": "UP",
      "details": {
        "clusterId": "kafka-cluster-1",
        "nodeCount": 3
      }
    },
    "trafficLight": {
      "status": "UP",
      "details": {
        "totalIntersections": 10,
        "activeIntersections": 8,
        "totalTrafficLights": 40
      }
    }
  }
}
```

### System Events API

#### Get System Events
```http
GET /api/events?page=0&size=50&eventType=STATE_CHANGE
```

#### Get Event by ID
```http
GET /api/events/{id}
```

### Analytics API

#### Get Traffic Metrics
```http
GET /api/analytics/metrics?intersectionId={id}&startDate=2024-01-01&endDate=2024-01-31
```

#### Get State Transition Statistics
```http
GET /api/analytics/transitions?intersectionId={id}
```

### Configuration API

#### Get System Configuration
```http
GET /api/config
```

#### Update Configuration
```http
PUT /api/config
Content-Type: application/json

{
  "defaultRedDuration": 30,
  "defaultYellowDuration": 5,
  "defaultGreenDuration": 25
}
```

## Error Codes

### HTTP Status Codes

| Status Code | Description |
|------------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 204 | No Content - Request successful, no content to return |
| 400 | Bad Request - Invalid request parameters |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource conflict (e.g., duplicate name) |
| 422 | Unprocessable Entity - Validation error |
| 429 | Too Many Requests - Rate limit exceeded |
| 500 | Internal Server Error - Server error |
| 503 | Service Unavailable - Service temporarily unavailable |

### Error Response Format

All error responses follow this format:

```json
{
  "success": false,
  "message": "Error message",
  "error": {
    "code": "ERROR_CODE",
    "details": "Detailed error description",
    "field": "fieldName",
    "timestamp": "2024-01-27T10:30:00.000Z"
  },
  "timestamp": "2024-01-27T10:30:00.000Z",
  "correlationId": "abc123"
}
```

### Application Error Codes

| Error Code | HTTP Status | Description |
|-----------|-------------|-------------|
| TRAFFIC_LIGHT_NOT_FOUND | 404 | Traffic light not found |
| INTERSECTION_NOT_FOUND | 404 | Intersection not found |
| SEQUENCE_NOT_FOUND | 404 | Light sequence not found |
| INVALID_STATE_TRANSITION | 422 | Invalid state transition attempted |
| CONFLICTING_DIRECTIONS | 409 | Conflicting directions have green lights |
| CONCURRENT_MODIFICATION | 409 | Resource was modified by another request |
| VALIDATION_ERROR | 422 | Input validation failed |
| DUPLICATE_NAME | 409 | Resource with same name already exists |
| RATE_LIMIT_EXCEEDED | 429 | Too many requests |
| KAFKA_PUBLISH_ERROR | 500 | Failed to publish event to Kafka |
| DATABASE_ERROR | 500 | Database operation failed |

### Validation Error Response Example

```json
{
  "success": false,
  "message": "Validation failed",
  "error": {
    "code": "VALIDATION_ERROR",
    "details": "Invalid input parameters",
    "validationErrors": [
      {
        "field": "name",
        "message": "Name is required",
        "rejectedValue": null
      },
      {
        "field": "latitude",
        "message": "Latitude must be between -90 and 90",
        "rejectedValue": 100.5
      }
    ]
  },
  "timestamp": "2024-01-27T10:30:00.000Z",
  "correlationId": "abc123"
}
```

## Rate Limiting

The API implements rate limiting to prevent abuse:

- **Default Limit**: 200 requests per second per IP address
- **Rate Limit Headers**:
  - `X-RateLimit-Limit`: Maximum requests allowed
  - `X-RateLimit-Remaining`: Remaining requests in current window
  - `X-RateLimit-Reset`: Time when the rate limit resets

When rate limit is exceeded, the API returns HTTP 429 with:

```json
{
  "error": "Too many requests. Please try again later."
}
```

## Pagination

List endpoints support pagination with the following query parameters:

- `page`: Page number (0-indexed, default: 0)
- `size`: Page size (default: 20, max: 100)
- `sort`: Sort field and direction (e.g., `name,asc` or `createdAt,desc`)

**Pagination Response**:

```json
{
  "success": true,
  "data": {
    "content": [...],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "sorted": true,
        "unsorted": false
      }
    },
    "totalElements": 100,
    "totalPages": 5,
    "last": false,
    "first": true,
    "numberOfElements": 20
  }
}
```

## Correlation ID Tracking

Every request and response includes a correlation ID for tracing:

- **Request Header**: `X-Correlation-ID` (optional)
- **Response Header**: `X-Correlation-ID` (always present)

If not provided in the request, the system generates a unique correlation ID automatically.

## Event-Driven Architecture

The system publishes events to Kafka for:

- State changes
- Intersection updates
- System health events

Events can be consumed by external systems for real-time monitoring and analytics.

## Swagger UI

Interactive API documentation is available at:

```
http://localhost:9900/swagger-ui.html
```

OpenAPI specification (JSON):

```
http://localhost:9900/v3/api-docs
```

## Support

For API support, contact:
- Email: support@trafficlight.com
- Documentation: https://docs.trafficlight.com
