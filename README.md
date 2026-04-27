# Antigravity Store

A hyper-local inventory management application that combines a Spring Boot backend with a React + Vite frontend.

## Project Summary

The project enables nearby search for grocery and convenience items using real location data. Users can upload product images, create inventory items tied to store coordinates, and view nearby products in a mobile-friendly frontend.

### Backend
- Spring Boot 3.2.0, Java 21
- H2 in-memory database for development
- Spring Data JPA for persistence
- Spring Security with open public search and item APIs
- OpenAPI / Swagger support via SpringDoc

### Frontend
- React + TypeScript + Vite
- Tailwind CSS for UI styling
- React Query for data fetching
- Custom hooks for geolocation and debounced search
- Proxy configured to `/api` for backend API calls

## Key Technical Implementations

### Image Handling
- Uploaded image files are stored in `backend/src/main/resources/static/images/`
- Spring Boot automatically serves static files from `src/main/resources/static`
- The backend returns image URLs such as `http://localhost:8081/images/<filename>` so the frontend can display them directly
- This maps physical file storage to a public web route without exposing internal storage logic

### Geospatial Search
- Nearby search uses a Haversine-style distance formula in the backend
- The repository query filters items by kilometers from the user coordinates:
  - `6371 * acos(cos(radians(:lat)) * cos(radians(i.latitude)) * cos(radians(i.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(i.latitude)))`
- Search results include `distance` and are sorted by proximity

### Debounced Search
- The frontend uses `frontend/src/hooks/useDebounce.ts`
- This delays API calls until the user stops typing for 400ms
- It prevents one backend request per keystroke, reducing load and improving responsiveness

## Backend Architecture

### Global Exception Handling
- Implemented in `backend/src/main/java/com/antigravity/exception/GlobalExceptionHandler.java`
- Uses `@ControllerAdvice` to return consistent API error responses, such as `404 Item Not Found`

### Data Validation
- Uses Jakarta Validation with `@Valid`, `@NotNull`, `@DecimalMin`, and other annotations
- Ensures request payloads are valid before processing
- Prevents invalid values such as negative price or missing required fields

### DTO Pattern
- Uses `InventoryItemDTO` instead of exposing JPA entities directly to the frontend
- This provides a layer of abstraction between internal database models and API payloads
- It also helps avoid accidentally leaking sensitive or internal fields

## Data Flow Explanation

### The “Handshake” (Step 2)
- The frontend sends a JSON payload for a new product item
- Backend receives it as `InventoryItemDTO`
- Backend assigns a UUID and saves it to the H2 database
- The item is stored with fields like `name`, `price`, `stockQuantity`, `imageUrl`, and coordinates

### The “Discovery” (Step 3)
- The frontend asks `GET /api/v1/items/search?lat=...&lng=...&radius=...`
- Backend calculates distance for all stored items using geospatial math
- Backend filters items outside the requested radius
- Backend returns matching items as an array ordered by nearest first

## Roadmap / Future Enhancements

- **Cloud Storage:** Transition local file storage to AWS S3 or Cloudinary for production scalability
- **Caching:** Add Redis caching for frequent `Nearby Search` queries to reduce database load
- **Authentication:** Expand Spring Security to include JWT-based login for store owners and admin functionality

## How to Run

### Backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Test the API
- Use `requests.http` to upload images, create items, and search nearby products

## Notes
- The project uses `frontend/src/components/ProductCard.tsx` to render item cards
- The search page is implemented in `frontend/src/pages/Home.tsx`
- Geolocation is handled in `frontend/src/hooks/useGeolocation.ts`
