(ns pollen-cast.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [pollen-cast.preprocess :as prep]
            [pollen-cast.allergens :as allergens]
            [pollen-cast.advice :as advice]
            [pollen-cast.calendar :as calendar]))

;; ---- PREPROCESS TESTS ----

(deftest test-normalize
  (testing "Normalize divides by std and subtracts mean"
    (is (= 0.0 (prep/normalize 4.0 4.0 1.0)))
    (is (= 1.0 (prep/normalize 5.0 4.0 1.0)))
    (is (= -1.0 (prep/normalize 3.0 4.0 1.0)))))

(deftest test-denormalize
  (testing "Denormalize is inverse of normalize"
    (is (= 5.0 (prep/denormalize 1.0 4.0 1.0)))
    (is (= 4.0 (prep/denormalize 0.0 4.0 1.0)))))

(deftest test-cyclic-encode
  (testing "Cyclic encode returns two values between -1 and 1"
    (let [[s c] (prep/cyclic-encode 6 12)]
      (is (= 2 (count [s c])))
      (is (<= -1.0 s 1.0))
      (is (<= -1.0 c 1.0))))
  (testing "January and December are close in cyclic encoding"
    (let [[s1 c1] (prep/cyclic-encode 1 12)
          [s12 c12] (prep/cyclic-encode 12 12)
          dist (Math/sqrt (+ (Math/pow (- s1 s12) 2)
                             (Math/pow (- c1 c12) 2)))]
      (is (< dist 0.6)))))

(deftest test-date-features
  (testing "Date features returns 6 values"
    (let [features (prep/date-features "2024-04-15")]
      (is (= 6 (count features)))))
  (testing "All date features are between -1 and 1"
    (let [features (prep/date-features "2024-04-15")]
      (is (every? #(<= -1.0 % 1.0) features)))))

(deftest test-active-season
  (testing "March is active season"
    (is (prep/active-season? {:date "2024-03-15" :values []})))
  (testing "October is active season"
    (is (prep/active-season? {:date "2024-10-01" :values []})))
  (testing "January is NOT active season"
    (is (not (prep/active-season? {:date "2024-01-15" :values []}))))
  (testing "December is NOT active season"
    (is (not (prep/active-season? {:date "2024-12-01" :values []})))))

;; ---- ADVICE TESTS ----

(deftest test-pollen-category
  (testing "Value 0 is :none"
    (is (= :none (advice/pollen-category 0.0 :very-high))))
  (testing "Value 65 is :medium for normal allergens"
    (is (= :medium (advice/pollen-category 65.0 :moderate))))
  (testing "Value 100 is :high"
    (is (= :high (advice/pollen-category 100.0 :moderate))))
  (testing "Ambrosia has lower threshold - 30 is :medium"
    (is (= :medium (advice/pollen-category 30.0 :very-high))))
  (testing "Value 50 is :low for normal allergens (threshold is 60)"
    (is (= :low (advice/pollen-category 50.0 :moderate)))))

(deftest test-individual-risk
  (testing "Value under 1.0 is always :none"
    (is (= :none (advice/individual-risk 0.5 :very-high)))
    (is (= :none (advice/individual-risk 0.0 :very-high))))
  (testing "High pollen + very high potency = very high risk"
    (is (= :very-high (advice/individual-risk 150.0 :very-high))))
  (testing "Low pollen + low potency = low risk"
    (is (= :low (advice/individual-risk 10.0 :low))))
  (testing "Medium pollen + high potency = high risk"
    (is (= :high (advice/individual-risk 70.0 :very-high)))))

(deftest test-risk-rank
  (testing "Very high has highest rank"
    (is (= 4 (advice/risk-rank :very-high))))
  (testing "None has lowest rank"
    (is (= 0 (advice/risk-rank :none))))
  (testing "Ranks are ordered correctly"
    (is (> (advice/risk-rank :very-high)
           (advice/risk-rank :high)
           (advice/risk-rank :medium)
           (advice/risk-rank :low)
           (advice/risk-rank :none)))))

;; ---- CALENDAR TESTS ----

(deftest test-active-this-month
  (testing "April is active for March-June season"
    (is (calendar/active-this-month? "March - June" 4)))
  (testing "January is not active for March-June season"
    (is (not (calendar/active-this-month? "March - June" 1))))
  (testing "October is active for July-October season"
    (is (calendar/active-this-month? "July - October" 10))))

;; ---- ALLERGENS TESTS ----

(deftest test-allergen-info-complete
  (testing "All pollen species have allergen info"
    (doseq [species prep/pollen-species]
      (is (not (nil? (get allergens/allergen-info species)))
          (str "Missing allergen info for: " species))))
  (testing "All allergen info has required fields"
    (doseq [[species info] allergens/allergen-info]
      (is (:name-en info) (str "Missing name-en for: " species))
      (is (:name-sr info) (str "Missing name-sr for: " species))
      (is (:potency info) (str "Missing potency for: " species))
      (is (:season info)  (str "Missing season for: " species)))))

(deftest test-pollen-species-count
  (testing "There are exactly 26 pollen species"
    (is (= 26 (count prep/pollen-species)))))
