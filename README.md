# 🚦 Rate Limiting with Custom Spring Filter

This project includes a custom **rate limiting filter** built with Spring Boot and Spring Security. The logic enforces **per-user request limits** within a configurable time window.

---
👍 Getting Started

Make sure you have Java 17+ and Maven installed.

✈️ Steps to Start the Project

Clone the repository:

git clone 
cd <your-project-directory>

Install dependencies:

mvn clean install

Run the application:
```
src/main/java/com/kayode/ratelimiter/RatelimiterApplication.java
```
The server should start on http://localhost:8080

## 📀 Request Flow Overview

1. **Incoming HTTP request** is authenticated via Spring Security.
2. A custom filter (`RateLimitingFilter`) intercepts the request after authentication.
3. If the principal is a `Long` user ID:

    * The filter delegates the decision to a `RateLimiter` implementation.
4. The current implementation, `InMemoryRateLimiter`, checks if the user has exceeded their allowed request count.
5. If allowed, the request proceeds to the controller.
6. If not, a `429 TOO MANY REQUESTS` response is returned with a message:

   ```
   Rate limit exceeded. Try again later.
   ```

---

## 🧐 Core Rate Limiting Logic

The core rate-limiting logic is implemented in:

```
src/main/java/.../InMemoryRateLimiter.java
```

* Uses a **`ConcurrentHashMap<Long, Deque<Instant>>`** to track request times per user.
* The `Deque` acts as a FIFO queue to efficiently:

    * Remove old timestamps outside the time window.
    * Append new request timestamps.
* Synchronization is done **per user**, so requests from one user don't block others.

This class implements the `RateLimiter` interface, making it easy to test and replace (e.g., with a Redis-based limiter).

Please inspect this class to see that it follows the requirements given and the tests are below.

---

## ✅ Testing

Unit tests are located at:

```
src/test/java/.../InMemoryRateLimiterTest.java
```

Tests follow the **Given–When–Then** naming pattern and verify:

* ✅ Requests below limit are allowed
* ❌ Requests above limit are rejected
* ⏱ Requests are allowed again after time window resets
* 🧵 Multi-threaded access is thread-safe
* 👤 One user's rate limit does not affect another user

---

## ⚙️ Configuration

You can configure rate limiting in your `application.properties` file:

```
rate-limit.max-requests=2
rate-limit.window=10s
```

| Property                  | Description                   |
| ------------------------- | ----------------------------- |
| `rate-limit.max-requests` | Max allowed requests per user |
| `rate-limit.window`       | Time window duration          |

> 🧪 **Current setting**: 2 requests per 10 seconds — easy to test locally. Adjust as needed in production.

---

## 🔁 Example CURL for Testing

To test locally with this configuration, run:

```bash
curl http://localhost:8080/users/1
```

Then run it multiple times within 10 seconds. After the second request, you'll get:

```http
HTTP/1.1 429 Too Many Requests
Rate limit exceeded. Try again later.
```

---

## 🧹 Key Classes

| File                           | Purpose                                               |
| ------------------------------ | ----------------------------------------------------- |
| `RateLimitingFilter.java`      | Intercepts requests and applies rate limiting         |
| `InMemoryRateLimiter.java`     | Thread-safe in-memory implementation of `RateLimiter` |
| `RateLimitProperties.java`     | Binds rate limit config from properties file          |
| `InMemoryRateLimiterTest.java` | Thorough unit tests with concurrency and edge cases   |

---

## 📌 Final Notes

This filter-based rate limiter is ideal for simple deployments. For distributed environments, consider replacing `InMemoryRateLimiter` with a Redis or token-bucket implementation.

Happy throttling! 🚀
