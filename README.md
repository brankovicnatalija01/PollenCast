# PollenCast - Pollen Forecasting System for Serbia

**PollenCast** is a Clojure-based console application designed to monitor and predict pollen levels across Serbia. By leveraging data from the Serbian Environmental Protection Agency (SEPA) and a custom-trained Neural Network, the application provides personalized allergy risks, 7-day forecasts, and actionable insights for allergy sufferers.

---

## Key Features

- **Real-time Monitoring:** Fetches the latest pollen data for 26 different allergens from 12 locations in Serbia.
- **AI-Powered Forecasts:** Uses a Multilayer Perceptron (MLP) neural network to predict pollen concentrations for the next 21 days.
- **Personalized Profiles:** Create a personal account to track specific allergens and set your home location.
- **Personalized Recommendations:** A "Should I go out today?" feature that calculates risk based on your specific triggers and plant allergenicity.
- **Allergy Calendar:** A visual monthly and yearly guide to your specific allergens and their blooming seasons.
- **Top 5 Cities With Lowest Pollen:** Identifies cities in Serbia with the lowest total pollen levels for the day.
- **Educational Database:** Detailed information, symptoms, and advice for 26 types of allergenic plants.

---

## Neural Network Architecture

To overcome the delay in manual pollen measurements, PollenCast utilizes a **MLP (Multilayer Perceptron)** model:

- **Structure:** 3 hidden layers (512, 256, and 128 neurons).
- **Input:** 30 days of historical concentrations + temporal features (month, day of year, day of week) encoded via sine/cosine functions.
- **Output:** Predictions for the next 21 days.
- **Performance:** Achieves a **Root Mean Square Error (RMSE)** of approximately $2\ pz/m^3$.
- **Training Data:** Historical datasets from 2019 to 2025 provided by [data.gov.rs](https://data.gov.rs).

---

## Technical Stack

- **Language:** [Clojure](https://clojure.org/)
- **Deep Learning:** [Deep Diamond](https://github.com/uncomplicate/deep-diamond) (utilizing **Intel MKL**)
- **Linear Algebra:** [Neanderthal](https://neanderthal.uncomplicate.org/)
- **Database:** [Codax](https://github.com/thegeez/codax) (Local Key-Value store for user profiles)
- **API Communication:** `clj-http` & `Cheshire` (JSON parsing)

---

## Use Cases

1.  **Registration & Login:** Securely store your location and allergy preferences locally.
2.  **Current Pollen Levels:** View predicted levels for today, categorized by severity (NONE, LOW, MEDIUM, HIGH, VERY HIGH).
3.  **7-Day Forecast:** Plan your week ahead with AI-generated predictions starting from the current date.
4.  **"Should I go out?":** Get a personalized safety assessment based on the worst-performing allergen in your profile.
5.  **Allergy Calendar:** Track which of your allergens are active during the current month or throughout the year.
6.  **Info Hub:** Access practical advice and botanical facts for any of the 26 monitored species.

---

## Data Source

The application consumes Open Data from the Serbian Environmental Protection Agency via the **dat.gov.rs** platform.

- **API Endpoints:** `GET /api/opendata/pollens/{year}/` and `GET /api/opendata/locations/`
- **Metrics:** Measured in $pz/m^3$ (pollen grains per cubic meter).

---

## Installation & Usage

### Prerequisites

- **Java 11+** (JDK)
- **Leiningen** (Clojure project automation)
- **Intel MKL** (Required for high-performance math operations used by Deep Diamond)

### Running the App

1.  **Clone the repository:**

    ```bash
    git clone [https://github.com/your-username/PollenCast.git](https://github.com/your-username/PollenCast.git)
    ```

2.  **Navigate to the project directory:**
    The application must be run from the `pollen-cast` subfolder:

    ```bash
    cd PollenCast/pollen-cast
    ```

3.  **Run the application:**
    ```bash
    lein run
    ```
