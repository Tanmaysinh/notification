# VasyERP Notification System

# Setup Steps

## Prerequisites

Before running the project, ensure the following software is installed:

- Node.js v22 or later
- Java 21
- PostgreSQL
- RabbitMQ
- Maven 3.9+
- Git

--------------------------------------------------

# Frontend Setup

## 1. Verify Node Version

Run:

node -v

The version should be 22.x.x or higher.

If Node.js is not installed or is an older version, install NVM.

Install Node 22

nvm install 22

Use Node 22

nvm use 22

Verify

node -v

--------------------------------------------------

## 2. Install Dependencies

npm install

--------------------------------------------------

## 3. Start Frontend

npm run dev

The frontend will start on

http://localhost:5173

Note:
The port may vary depending on your environment.
Check the terminal after running `npm run dev`.

--------------------------------------------------

# Backend Setup

## Requirements

- Java 21
- PostgreSQL
- RabbitMQ
- Maven

--------------------------------------------------

## PostgreSQL Configuration

Create a PostgreSQL database.

Example

Database Name:
vasyerp_task

Update application.properties according to your environment.

spring.datasource.url=jdbc:postgresql://localhost:5432/vasyerp_task
spring.datasource.username=tanmaysinh
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

#spring.data.mongodb.uri=mongodb://localhost:27017/notification_db

app.jwt.secret=PppppppppppppppLLlllllllllllllll
app.jwt.expiration-minutes=60

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

app.rabbitmq.notification-exchange=notification.delayed.exchange
app.rabbitmq.notification-queue=notification.queue
app.rabbitmq.notification-routing-key=notification.send

app.rabbitmq.status-exchange=status.exchange
app.rabbitmq.status-queue=status.queue
app.rabbitmq.status-routing-key=contact.status

Modify these values according to your local PostgreSQL and RabbitMQ configuration.

--------------------------------------------------

# RabbitMQ Setup

If RabbitMQ is not installed locally, you can run it using Docker.

docker run -d \
  --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:4-management

RabbitMQ Management Console

http://localhost:15672

Default Credentials

Username : guest
Password : guest

--------------------------------------------------

# Run Backend

Option 1

mvn spring-boot:run

Option 2

mvn clean install

java -jar target/notification-app-1.0.0.jar

--------------------------------------------------

# Running the Application

After both frontend and backend are running,

Open

http://localhost:5173

(or the URL shown after running npm run dev)

--------------------------------------------------

# First Time Usage

There is no default admin account.

Follow these steps:

1. Sign Up
2. Login
3. Add Contacts
4. Create Notification Templates
5. (Optional) Create Campaign
6. Send Notification
7. View Reports

Application Flow

Signup
    ↓
Login
    ↓
Create Contacts
    ↓
Create Templates
    ↓
(Optional) Create Campaign
    ↓
Send Notification
    ↓
Receive Request ID
    ↓
Open Report Page
    ↓
Track Status for each Contact and each Channel

==================================================
PROJECT ARCHITECTURE
==================================================

The application follows a modular architecture with a React frontend and a Spring Boot backend.

Frontend is responsible for user interaction, while the backend handles authentication, business logic, scheduling, notification processing, retry mechanisms, and reporting.

High Level Architecture

                    +----------------+
                    |    Frontend    |
                    | (React + Vite) |
                    +-------+--------+
                            |
                            |
                      Secure API Calls
                            |
                            |
                    +-------v--------+
                    | Spring Boot API|
                    +-------+--------+
                            |
          -----------------------------------------
          |          |          |                 |
          |          |          |                 |
     PostgreSQL   RabbitMQ   Scheduler      JWT/Auth
          |
          |
     Notification Data

Major Modules

- Authentication
- Dashboard
- Contact Management
- Template Management
- Campaign Management
- Notification Processing
- Reports

--------------------------------------------------
AUTHENTICATION
--------------------------------------------------

Authentication provides

- User Registration
- Login
- JWT Authentication
- Secure Communication

Secure Handshake

Before login, the frontend establishes a secure communication channel.

The application uses ECDH (Elliptic Curve Diffie-Hellman) to derive a shared encryption key between the browser and server.

Flow

1. Browser generates a temporary ECDH key pair.
2. Browser sends only its Public Key.
3. Server returns its Public Key and Session ID.
4. Both derive the same shared secret independently.
5. HKDF converts the shared secret into an AES-256-GCM key.
6. All request and response bodies are encrypted using the derived AES key.

The AES key is never transmitted over the network.

After successful login, the backend generates a JWT token used for authentication of future API requests.

--------------------------------------------------
DASHBOARD
--------------------------------------------------

The dashboard provides an overview of notification activity.

It displays

- Total Requests
- Total Notifications
- Channel-wise Summary
- Delivery Statistics
- Failed Notifications

Summary data is pre-computed and stored in the database instead of being calculated on every request for better performance.

--------------------------------------------------
CONTACT MANAGEMENT
--------------------------------------------------

Stores recipient information.

Features

- Add Contact
- Edit Contact
- Delete Contact

Each contact contains

- Name
- Email Address
- Phone Number
- Device Token

--------------------------------------------------
TEMPLATE MANAGEMENT
--------------------------------------------------

Templates are reusable notification messages.

Supported template types

- SMS
- Email
- Push Notification

Users can create templates once and reuse them while sending notifications.

--------------------------------------------------
CAMPAIGN MANAGEMENT
--------------------------------------------------

Campaigns combine multiple templates into a reusable notification configuration.

Features

- Create Campaign
- Edit Campaign
- Delete Campaign

A campaign may contain

- SMS Template
- Email Template
- Push Template

This reduces repetitive configuration while sending notifications.

--------------------------------------------------
SEND NOTIFICATION
--------------------------------------------------

Notifications can be sent

1. Using an existing Campaign

or

2. Using custom selected templates.

Users can

- Select Contacts
- Select Notification Channels
- Choose Templates
- Schedule Notification Time

After submission, a unique Request ID is generated and the user is redirected to the Reports page.

--------------------------------------------------
NOTIFICATION PROCESSING
--------------------------------------------------

RabbitMQ does not provide delayed scheduling in the Community Edition used for this project.

Therefore, delayed notifications are handled at the application level.

Notification Flow

User Sends Notification
            │
            ▼
Save Request in Database
(Status = PENDING)
            │
            ▼
Scheduler (Every 30 Seconds)
            │
            ▼
Find Eligible Requests
(schedule_time <= current time)
            │
            ▼
Update Status = QUEUED
            │
            ▼
Publish Request to RabbitMQ
            │
            ▼
RabbitMQ Consumer
            │
            ▼
Process Notifications

--------------------------------------------------
RABBITMQ CONSUMER
--------------------------------------------------

The notification consumer listens for queued notification requests.

For every request

1. Load all selected contacts.
2. Determine the selected notification channels.
3. Send notifications asynchronously using CompletableFuture.
4. Store notification status for every contact and every channel.
5. After all contacts are processed, mark the request as COMPLETED.

Parallel processing using CompletableFuture significantly reduces notification processing time for large recipient lists.

--------------------------------------------------
STATUS PROCESSING
--------------------------------------------------

A separate status queue simulates notification provider callbacks.

For every status update

- Update request status.
- Update contact status.
- Store complete attempt history.
- If delivery fails and retry attempts are available,
  enqueue the notification again.

Retry logic continues until

- Notification succeeds, or
- Maximum retry count is reached.

--------------------------------------------------
SUMMARY SCHEDULER
--------------------------------------------------

A dedicated scheduler periodically scans completed notification requests.

For completed requests

- Calculate channel-wise statistics.
- Calculate delivery counts.
- Calculate failure counts.
- Calculate retry counts.
- Store aggregated summary in the database.

This avoids expensive aggregation queries every time the Dashboard or Reports page is opened.

--------------------------------------------------
REPORTS
--------------------------------------------------

Reports provide complete tracking of every notification request.

Request Level

Displays

- Request ID
- Campaign
- Schedule Time
- Overall Status
- Total Recipients

Contact Level

Each request expands to show every selected contact.

For every contact

- SMS Status
- Email Status
- Push Status

Each channel displays

- Notification Content
- Complete Attempt History
- Retry Count
- Current Status
- Retry Option (when eligible)

Report Structure

Request
├── Contact A
│   ├── SMS
│   ├── Email
│   └── Push
│
└── Contact B
    ├── SMS
    ├── Email
    └── Push


==================================================
DATABASE SCHEMA EXPLANATION
==================================================

The application uses a relational database to store master data, notification requests, campaigns, and notification status tracking.

The design separates frequently changing master data from historical notification records to preserve the exact content and recipient information used when a notification was sent.

--------------------------------------------------
1. USER_MASTER
--------------------------------------------------

Stores application user information used for authentication.

Columns

- user_id
- name
- email
- password

Notes

- Password is stored in hashed format (BCrypt).
- Used only for application login and authentication.
- Password is never stored in plain text.

--------------------------------------------------
2. CONTACT_MASTER
--------------------------------------------------

Stores recipient information.

Columns

- contact_id
- name
- email
- phone_number
- device_token

Purpose

A contact may receive notifications through one or more channels.

- Email
- SMS
- Push Notification

The appropriate field is used depending on the selected notification channel.

--------------------------------------------------
3. TEMPLATE MASTER TABLES
--------------------------------------------------

Separate master tables are maintained for each notification channel.

SMS_TEMPLATE_MASTER

- template_id
- name
- content

EMAIL_TEMPLATE_MASTER

- template_id
- name
- content

PUSH_TEMPLATE_MASTER

- template_id
- name
- content

Purpose

Templates provide reusable notification content so users do not have to type the same message repeatedly.

--------------------------------------------------
4. CAMPAIGN_MASTER
--------------------------------------------------

Stores reusable notification campaigns.

Columns

- campaign_id
- name
- sms_template_id
- email_template_id
- push_template_id

Purpose

A campaign combines templates from multiple channels into a single reusable configuration.

Instead of selecting templates every time, users can simply choose a campaign while sending notifications.

--------------------------------------------------
5. NOTIFICATION_REQUEST_MASTER
--------------------------------------------------

A new record is created whenever the user submits a notification request.

Columns

- request_id
- campaign_id
- sms_template
- email_template
- push_template
- recipient_count
- schedule_time
- created_at
- status
- retry_attempt
- channel_wise_retry_count
- total_channel_wise_status_count
- summary_fetched

Purpose

This table stores information about a notification request as a whole.

Unlike Campaign Master, this table stores the actual template content (JSON) instead of foreign keys.

Reason

Templates may be modified after notifications have been sent.

If only template IDs were stored, previously sent notifications would display updated template content instead of the original message.

By storing the template snapshot, historical reports always show the exact notification content that was sent.

recipient_count

Stores the total number of selected recipients.

Used for reporting and dashboard summaries.

retry_attempt

Defines the maximum number of retry attempts for failed notifications.

Example

3

means each failed notification can be retried up to three times.

channel_wise_retry_count

Stores the total retry count for each notification channel.

Example

{
  "sms": 4,
  "email": 1,
  "push": 0
}

This is used for dashboard statistics and reporting.

total_channel_wise_status_count

Stores aggregated notification status counts for the request.

Example

{
  "sms": {
    "SENT": 10,
    "DELIVERED": 8,
    "FAILED": 2
  },
  "email": {
    "DELIVERED": 10
  },
  "push": {
    "FAILED": 1,
    "DELIVERED": 9
  }
}

Purpose

Instead of recalculating statistics every time a report is opened, summary counts are maintained for faster reporting.

summary_fetched

Indicates whether all notification status updates have been processed and the request summary has been finalized.

--------------------------------------------------
6. REQUEST_STATUS_MASTER
--------------------------------------------------

Stores notification status for every contact within a request.

Columns

- id
- request_id
- contact_id
- channel_wise_status_json
- updated_at

Purpose

Each row represents one recipient within a notification request.

The detailed status of every notification channel is stored as JSON.

Example

{
  "sms": {
    "status": [
      "FAILED",
      "DELIVERED"
    ],
    "retryCount": 1,
    "userData": "11111111"
  },
  "email": {
    "status": [
      "DELIVERED"
    ],
    "retryCount": 0,
    "userData": "abc@gmail.com"
  },
  "push": {
    "status": [
      "FAILED",
      "DELIVERED"
    ],
    "retryCount": 1,
    "userData": "qazxswwertyuo"
  }
}

status

Stores the complete history of every delivery attempt.

Example

SMS

[
    "FAILED",
    "FAILED",
    "DELIVERED"
]

This allows the application to display the complete retry history instead of only the latest status.

retryCount

Stores how many retries have already been performed for that specific channel.

userData

Stores the actual recipient value used while sending the notification.

Examples

SMS

"9876543210"

Email

"user@example.com"

Push

"device_token_xyz"

Reason

Contact information may change after the notification has been sent.

For example,

A user changes their email address from

old@example.com

to

new@example.com

Historical reports should still display that the notification was originally sent to

old@example.com

Therefore, the actual recipient data is stored with the notification status instead of always reading the latest value from CONTACT_MASTER.





==================================================
ASSUMPTIONS MADE
==================================================

### 1. Single Backend Service

For this assignment, all API endpoints, schedulers, RabbitMQ producers, and RabbitMQ consumers are implemented within a single Spring Boot application.

This simplifies deployment and reduces infrastructure requirements for moderate workloads.

In a production environment with higher traffic, notification consumers and background processors can be separated into independent microservices that consume messages from RabbitMQ. This allows the notification processing layer to scale independently from the API layer.

--------------------------------------------------

### 2. PostgreSQL Used for Transaction Data

Although MongoDB is generally well suited for storing flexible JSON documents such as notification status history, PostgreSQL was chosen for this assignment.

Reasons:

- Existing master data (Users, Contacts, Templates, Campaigns) is relational in nature.
- PostgreSQL provides JSON/JSONB support, allowing flexible notification status data to be stored without introducing an additional database.
- Using a single database simplifies deployment, configuration, and maintenance.
- For the expected workload of this assignment, PostgreSQL provides sufficient performance.

For larger-scale production systems with very high notification volumes, MongoDB (or another document database) could be introduced specifically for notification transaction history while keeping master data in PostgreSQL.

--------------------------------------------------

### 3. Application-Level Scheduling

RabbitMQ Community Edition does not provide built-in delayed message scheduling.

Therefore, scheduled notifications are managed at the application level.

Notification requests are initially stored with a PENDING status, and a scheduler running every 30 seconds identifies eligible requests and publishes them to RabbitMQ.

--------------------------------------------------

### 4. Summary Data is Precomputed

Dashboard statistics are periodically calculated and stored in the database instead of being generated on every request.

This reduces expensive aggregation queries and improves dashboard performance.

--------------------------------------------------

### 5. Historical Data Preservation

Notification templates and recipient information are copied into request-specific records when a notification is sent.

This ensures that historical reports always display the exact template content and recipient details used at the time of delivery, even if templates or contact information are modified later.





==================================================
IMPORTANT IMPLEMENTATION DETAILS
==================================================

### 1. Secure Communication

Sensitive API payloads are encrypted using an application-level encryption mechanism.

The frontend and backend establish a shared AES-256-GCM encryption key using an ECDH handshake and HKDF key derivation before any authentication request is sent.

All request and response bodies are encrypted using the derived AES key.

--------------------------------------------------

### 2. JWT Authentication

User authentication is handled using JWT tokens.

Passwords are stored using BCrypt hashing, and every authenticated request is validated through Spring Security.

--------------------------------------------------

### 3. Asynchronous Notification Processing

Notification sending is completely asynchronous.

After a request is accepted, it is queued through RabbitMQ and processed in the background without blocking the API response.

--------------------------------------------------

### 4. Thread Pool Based Processing

RabbitMQ listeners use a dedicated Thread Pool (ExecutorService) for processing notifications.

Instead of creating a new thread for every notification, worker threads are reused.

Benefits

- Better CPU utilization
- Controlled concurrency
- Prevents excessive thread creation
- Handles increasing load efficiently
- Improves overall application responsiveness

As notification volume increases, queued messages wait for an available worker thread instead of impacting API request processing.

--------------------------------------------------


### 5. Parallel Processing

Notifications for multiple recipients are processed concurrently using Java CompletableFuture.

This significantly reduces the total processing time when sending notifications to a large number of recipients.

--------------------------------------------------

### 6. Retry Mechanism

Each notification channel supports configurable retry attempts.

Failed notifications are automatically retried until either:

- Delivery succeeds, or
- The configured maximum retry count is reached.

Complete retry history is maintained for reporting purposes.

--------------------------------------------------

### 7. Historical Data Preservation

Notification requests store copies (snapshots) of templates and recipient information instead of relying solely on master tables.

This ensures reports always display the exact content and recipient details used when the notification was originally sent, even if templates or contacts are modified later.

--------------------------------------------------

### 8. Optimized Dashboard Reporting

Channel-wise delivery summaries are precomputed and stored in the database by a background scheduler.

This avoids expensive aggregation queries whenever the Dashboard or Reports page is opened.

--------------------------------------------------

### 9. Modular Architecture

The project is organized into independent modules such as Authentication, Contacts, Templates, Campaigns, Notification Processing, and Reports.

This separation improves maintainability and makes future feature additions easier.

--------------------------------------------------

### 10. Clean Layered Backend Design

The backend follows a layered architecture consisting of:

- Controller
- Service
- Repository
- Entity
- DTO
- Mapper
- Configuration

This keeps business logic separate from API and persistence layers, making the application easier to test and maintain.

--------------------------------------------------

### 10. Extensible Notification Design

Notification channels (SMS, Email, Push) are implemented in a way that allows additional channels (such as WhatsApp or Voice Calls) to be integrated with minimal changes to the existing architecture.