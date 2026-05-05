(ns pollen-cast.allergen-info
  (:require [pollen-cast.allergens :as allergens]
            [pollen-cast.model :as model]))

(def allergen-descriptions
  {:AMBROSIA      {:description "Ragweed is one of the most allergenic plants in Serbia and Europe. It produces large amounts of highly allergenic pollen that can travel hundreds of kilometers. Serbia is one of the invasion hotspots in Europe alongside Hungary and Croatia."
                   :symptoms    "Sneezing, runny nose, itchy eyes, asthma attacks, oral allergy syndrome with certain foods (celery, cucumbers, melons)"
                   :tips        "Pollen peaks at midday. Avoid outdoor activities in August-September mornings. Ragweed can cross-react with mugwort pollen."}
   :BETULA        {:description "Birch is one of the most common causes of spring allergies in Serbia. Its pollen can travel long distances and cross-react with certain foods."
                   :symptoms    "Hay fever, itchy eyes, runny nose, oral allergy syndrome with raw apples, pears, cherries and nuts"
                   :tips        "Cooking apples, nuts and stone fruits usually eliminates the cross-reaction. Birch cross-reacts with alder and hazel pollen."}
   :POACEAE       {:description "Grass pollen is the most common cause of summer allergies worldwide. There are hundreds of grass species but most share similar allergens."
                   :symptoms    "Sneezing, runny nose, itchy eyes, asthma"
                   :tips        "Grass pollen peaks in the morning. Mowing the lawn can trigger severe reactions. Consider wearing a mask when outdoors during season."}
   :ALNUS         {:description "Alder is one of the earliest spring pollinators, often starting in February. It cross-reacts with birch and hazel pollen."
                   :symptoms    "Hay fever, itchy eyes, runny nose"
                   :tips        "Alder season often surprises people as it starts very early. Monitor pollen forecasts from February onwards."}
   :ARTEMISIA     {:description "Mugwort is a common weed that pollinates in late summer. It cross-reacts with ragweed and certain foods."
                   :symptoms    "Hay fever, asthma, oral allergy syndrome with celery, carrots, fennel and spices"
                   :tips        "Avoid celery, carrots, fennel and spices during mugwort season. Mugwort cross-reacts with ragweed pollen."}
   :CORYLUS       {:description "Hazel is one of the first trees to pollinate in late winter, often alongside alder. It cross-reacts with birch pollen."
                   :symptoms    "Sneezing, runny nose, itchy eyes"
                   :tips        "Hazel cross-reacts with birch and alder. If allergic to birch, you may also react to hazel pollen."}
   :FRAXINUS      {:description "Ash tree is a common cause of spring allergies. It pollinates at the same time as birch and can cause strong reactions."
                   :symptoms    "Hay fever, itchy eyes, runny nose, asthma"
                   :tips        "Ash pollen season overlaps with birch. Combined exposure can intensify symptoms significantly."}
   :QUERCUS       {:description "Oak produces large amounts of pollen in spring. While less allergenic than birch, high concentrations can cause significant symptoms."
                   :symptoms    "Sneezing, runny nose, itchy eyes"
                   :tips        "Oak pollen season is long, lasting from April to June. Symptoms may persist for several weeks."}
   :PLATANUS      {:description "Plane tree is common in Serbian cities and parks. Its pollen and fine leaf hairs can cause strong allergic reactions."
                   :symptoms    "Hay fever, itchy eyes, skin irritation, asthma"
                   :tips        "Plane tree hairs can irritate skin and airways even without pollen. Avoid sitting under plane trees in spring."}
   :URTICACEAE    {:description "Nettle pollen is a common cause of summer allergies. Despite being a weed, it produces highly allergenic pollen with a very long season."
                   :symptoms    "Sneezing, runny nose, itchy eyes"
                   :tips        "Nettle has a very long season from May to October. Symptoms can persist throughout the entire summer."}
   :CHENOP/AMAR.  {:description "Goosefoot and pigweed are common weeds that pollinate throughout summer and autumn. They are found in urban areas and agricultural fields."
                   :symptoms    "Hay fever, runny nose, itchy eyes"
                   :tips        "These weeds are common in urban areas. Season overlaps with ragweed in late summer."}
   :PINACEAE      {:description "Pine and fir trees produce large amounts of visible yellow pollen but are actually weakly allergenic. Most symptoms attributed to pine are caused by other concurrent pollinators."
                   :symptoms    "Mild hay fever in sensitive individuals"
                   :tips        "Despite visible yellow pollen clouds, pine is rarely a serious allergen. Check for other concurrent pollinators if you have symptoms."}
   :ACER          {:description "Maple trees pollinate early in spring. They are mildly allergenic and often overlooked as an allergy trigger."
                   :symptoms    "Mild hay fever, sneezing"
                   :tips        "Maple season is short but contributes to spring allergy load alongside other trees."}
   :FAGUS         {:description "Beech is a common forest tree in Serbia. Its pollen is moderately allergenic and pollinates in spring alongside other trees."
                   :symptoms    "Mild hay fever, sneezing, itchy eyes"
                   :tips        "Beech pollen season coincides with other spring trees. Combined exposure can worsen symptoms."}
   :CARPINUS      {:description "Hornbeam is related to birch and hazel and may cross-react with them. It is a common tree in Serbian forests."
                   :symptoms    "Hay fever, sneezing, itchy eyes"
                   :tips        "If allergic to birch or hazel, you may also react to hornbeam pollen due to cross-reactivity."}
   :SALIX         {:description "Willow is one of the earliest spring pollinators. It is commonly found near water and in parks throughout Serbia."
                   :symptoms    "Mild hay fever, sneezing"
                   :tips        "Willow season is short. Symptoms usually resolve quickly as the season ends in late May."}
   :MORACEAE      {:description "Mulberry trees are common in Serbian cities and villages. They produce moderately allergenic pollen in late spring."
                   :symptoms    "Hay fever, sneezing, itchy eyes"
                   :tips        "Mulberry pollen season is short but intense. Avoid areas with many mulberry trees during May."}
   :JUGLANS       {:description "Walnut trees are common in Serbia. Their pollen is moderately allergenic and pollinates in spring."
                   :symptoms    "Hay fever, sneezing, itchy eyes"
                   :tips        "Walnut pollen season is relatively short. Those allergic to birch may also react to walnut."}
   :TILIA         {:description "Lime (Linden) trees are very common in Serbian cities. Their flowers attract bees and produce mildly allergenic pollen."
                   :symptoms    "Very mild hay fever in sensitive individuals"
                   :tips        "Lime trees are rarely a significant allergy trigger. Their fragrant flowers are more of a pleasant feature of Serbian summers."}
   :POPULUS       {:description "Poplar trees produce visible white fluffy seeds that are often mistaken for pollen. The actual pollen is weakly allergenic."
                   :symptoms    "Mild irritation in sensitive individuals"
                   :tips        "The white fluff from poplar is seed material, not pollen. It rarely causes true allergic reactions."}
   :RUMEX         {:description "Sorrel is a common weed found in meadows and fields throughout Serbia. Its pollen is moderately allergenic throughout summer."
                   :symptoms    "Hay fever, sneezing, itchy eyes"
                   :tips        "Sorrel is common in rural areas and parks. Season overlaps with grass pollen in early summer."}
   :ULMACEAE      {:description "Elm trees pollinate very early in spring, often before leaves appear. They are moderately allergenic."
                   :symptoms    "Hay fever, sneezing, itchy eyes"
                   :tips        "Elm season is short and early. Symptoms usually resolve before the main spring allergy season begins."}
   :CANNABACEAE   {:description "Hemp and related plants produce pollen in summer. Hemp is grown in Serbia and can cause allergies in sensitive individuals."
                   :symptoms    "Mild hay fever, sneezing"
                   :tips        "Hemp pollen season is long. Rural areas with hemp cultivation may have higher pollen counts."}
   :CUPRESS/TAXA. {:description "Cypress and yew trees pollinate in late winter and early spring. They are moderately allergenic and common in Serbian parks and gardens."
                   :symptoms    "Hay fever, sneezing, itchy eyes"
                   :tips        "Cypress season starts early in February. Monitor pollen forecasts from late winter."}
   :CELTIS        {:description "Nettle tree (Hackberry) is found in warm areas of Serbia. It pollinates in spring and is weakly allergenic."
                   :symptoms    "Mild hay fever in sensitive individuals"
                   :tips        "Nettle tree pollen season is short. It rarely causes significant allergic reactions."}
   :PLANTAGO      {:description "Plantain is a common weed found in lawns and roadsides throughout Serbia. It produces moderately allergenic pollen throughout summer."
                   :symptoms    "Hay fever, sneezing, itchy eyes"
                   :tips        "Plantain is very common in urban areas. Season is long from May to September."}})

(defn potency-circle [potency]
  (case potency
    :very-high        "🔴 Very High"
    :high             "🟠 High"
    :moderate-to-high "🟡 Moderate to High"
    :moderate         "🟡 Moderate"
    :low-to-moderate  "🟢 Low to Moderate"
    :low              "🟢 Low"
    :very-low         "🟢 Very Low"
    "⚪ Unknown"))

(defn show-allergen-info [user]
  (println "\n======================================")
  (println "  ALLERGEN INFORMATION")
  (println "======================================")
  (println "\nSelect an allergen to learn more:")
  (println "")
  (println (format "%-4s %-25s | %-20s | %s" "#" "English" "Serbian" "Potency"))
  (println (apply str (repeat 70 "-")))
  (let [species-list (vec (keys allergens/allergen-info))
        ;; Sort by potency rank descending
        sorted-species (sort-by (fn [s]
                                  (let [info (get allergens/allergen-info s)]
                                    (- (case (:potency info)
                                         :very-high        6
                                         :high             5
                                         :moderate-to-high 4
                                         :moderate         3
                                         :low-to-moderate  2
                                         :low              1
                                         :very-low         0
                                         0))))
                                species-list)
        indexed (map-indexed vector sorted-species)]
    (doseq [[i species] indexed]
      (let [info (get allergens/allergen-info species)]
        (println (format "%-4s %-25s | %-20s | %s"
                         (str (inc i) ".")
                         (:name-en info)
                         (:name-sr info)
                         (potency-circle (:potency info))))))
    (println "\n0. Back")
    (print "\n> ")
    (flush)
    (let [input (read-line)
          n     (try (Integer/parseInt input) (catch Exception _ 0))]
      (when (and (> n 0) (<= n (count sorted-species)))
        (let [species (nth sorted-species (dec n))
              info    (get allergens/allergen-info species)
              desc    (get allergen-descriptions species)]
          (println "\n======================================")
          (println (str "  " (:name-en info) " | " (:name-sr info)))
          (println "======================================")
          (println (str "\nType:     " (clojure.string/capitalize (name (:type info)))))
          (println (str "Season:   " (:season info)))
          (println (str "Potency:  " (potency-circle (:potency info))))
          (when desc
            (println "\nAbout:")
            (println (str "  " (:description desc)))
            (println "\nSymptoms:")
            (println (str "  " (:symptoms desc)))
            (println "\nTips:")
            (println (str "  " (:tips desc)))))))))