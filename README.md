# Smart Campus Crowd Intelligence System

A group project that provides a campus crowd monitoring and analysis
dashboard for facilities such as the Library, Canteen, and Fees Office.

The system uses simulated crowd data to demonstrate real-time monitoring,
trend analysis, crowd prediction, waiting-time estimation, and
best-visiting-time recommendations.

## Overview

Students often do not know how crowded a campus facility is before visiting
it. This project aims to provide a centralized dashboard that displays
crowd information for different campus locations and provides useful
insights based on current and historical data.

The current implementation uses controlled simulated data for demonstration.
The system also includes a separate computer-vision component for
experimentation with people detection.

## Key Features

- Real-time simulated crowd monitoring
- Location-wise crowd information
- Crowd status classification: Low, Medium, and High
- Crowd trend detection
- Short-term crowd prediction
- Best visiting-time recommendation
- Waiting-time estimation
- Historical crowd-data analysis
- Simulated and Live system modes
- Interactive dashboard
- Multiple campus locations

## System Architecture

The application consists of three main components:

### 1. Frontend

The frontend provides the dashboard used to display crowd information,
statistics, trends, predictions, and location-wise insights.

### 2. Backend

The backend is implemented using Java and Spring Boot.

It follows a layered architecture:

- Controller layer
- Service layer
- Repository layer
- Model/Entity layer
- DTO layer

The backend provides REST APIs for retrieving crowd information,
predictions, best visiting times, and system-mode management.

### 3. Vision Component

The project contains a separate Python-based computer-vision component
using OpenCV and YOLO for experimentation with people detection.

The vision component is currently separate from the main simulated-data
workflow.

## Crowd Simulation

The current demonstration uses a controlled simulation engine implemented
in the Java backend.

Crowd values are generated according to:

- Campus location
- Current hour
- Previous crowd value
- Controlled random variation

The simulation updates periodically and maintains short rolling windows
for trend and prediction calculations.

Hourly aggregated crowd data can also be stored in the database.

## Prediction and Analysis

The system provides several data-driven insights:

- **Crowd Status:** Classifies crowd levels as Low, Medium, or High.
- **Trend:** Determines whether the current crowd level is increasing,
  decreasing, or stable.
- **Prediction:** Estimates the next crowd level using recent values.
- **Best Time:** Uses historical crowd data to identify quieter hours.
- **Waiting Time:** Estimates waiting time based on location-specific
  crowd and capacity logic.

## Technology Stack

### Backend
- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Maven
- Lombok

### Database
- MySQL

### Frontend
- React.js
- JavaScript
- HTML
- CSS
- Axios
- Chart.js
- React Router

### Computer Vision
- Python
- OpenCV
- YOLO

### Version Control
- Git
- GitHub

## Project Structure

```text
smart-campus-crowd-system/
│
├── backend/
│   ├── src/main/java/
│   │   └── com/smartcampus/crowd/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── model/
│   │       ├── repository/
│   │       ├── service/
│   │       └── util/
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   └── styles.css
│   └── package.json
│
├── vision/
│   ├── test.py
│   └── yolov8n.pt
│
├── Dockerfile
└── run_project.bat
