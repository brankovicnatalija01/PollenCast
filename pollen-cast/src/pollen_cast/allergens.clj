(ns pollen-cast.allergens)

(def allergen-info
  {:AMBROSIA      {:name-sr    "Ambrozija"
                   :name-en    "Ragweed"
                   :type       :weed
                   :season     "July - October"
                   :potency    :very-high}
   :PLANTAGO      {:name-sr    "Bokvica"
                   :name-en    "Plantain"
                   :type       :weed
                   :season     "May - September"
                   :potency    :low-to-moderate}
   :PINACEAE      {:name-sr    "Borovi/Jele"
                   :name-en    "Pine/Fir"
                   :type       :tree
                   :season     "March - Late May"
                   :potency    :low}
   :BETULA        {:name-sr    "Breza"
                   :name-en    "Birch"
                   :type       :tree
                   :season     "March - Early June"
                   :potency    :very-high}
   :ULMACEAE      {:name-sr    "Brest"
                   :name-en    "Elm"
                   :type       :tree
                   :season     "March - April"
                   :potency    :moderate}
   :FAGUS         {:name-sr    "Bukva"
                   :name-en    "Beech"
                   :type       :tree
                   :season     "March - May"
                   :potency    :low-to-moderate}
   :SALIX         {:name-sr    "Vrba"
                   :name-en    "Willow"
                   :type       :tree
                   :season     "March - Late May"
                   :potency    :low}
   :CARPINUS      {:name-sr    "Grab"
                   :name-en    "Hornbeam"
                   :type       :tree
                   :season     "March - May"
                   :potency    :low-to-moderate}
   :MORACEAE      {:name-sr    "Dud"
                   :name-en    "Mulberry"
                   :type       :tree
                   :season     "April - June"
                   :potency    :low-to-moderate}
   :ACER          {:name-sr    "Javor"
                   :name-en    "Maple"
                   :type       :tree
                   :season     "March - Late May"
                   :potency    :low}
   :FRAXINUS      {:name-sr    "Jasen"
                   :name-en    "Ash"
                   :type       :tree
                   :season     "March - Late May"
                   :potency    :moderate-to-high}
   :ALNUS         {:name-sr    "Jova"
                   :name-en    "Alder"
                   :type       :tree
                   :season     "February - Early April"
                   :potency    :very-high}
   :RUMEX         {:name-sr    "Kiselica"
                   :name-en    "Sorrel"
                   :type       :weed
                   :season     "May - September"
                   :potency    :moderate-to-high}
   :CANNABACEAE   {:name-sr    "Konoplja"
                   :name-en    "Hemp"
                   :type       :weed
                   :season     "May - September"
                   :potency    :low}
   :URTICACEAE    {:name-sr    "Kopriva"
                   :name-en    "Nettle"
                   :type       :weed
                   :season     "May - October"
                   :potency    :moderate-to-high}
   :CORYLUS       {:name-sr    "Leska"
                   :name-en    "Hazelnut"
                   :type       :tree
                   :season     "February - Early April"
                   :potency    :moderate-to-high}
   :TILIA         {:name-sr    "Lipa"
                   :name-en    "Lime"
                   :type       :tree
                   :season     "May - Late June"
                   :potency    :very-low}
   :JUGLANS       {:name-sr    "Orah"
                   :name-en    "Walnut"
                   :type       :tree
                   :season     "April - June"
                   :potency    :low-to-moderate}
   :ARTEMISIA     {:name-sr    "Pelen"
                   :name-en    "Mugwort"
                   :type       :weed
                   :season     "Late July - October"
                   :potency    :high}
   :CHENOP/AMAR.  {:name-sr    "Pepeljuge/Stirovi"
                   :name-en    "Goosefoot/Pigweed"
                   :type       :weed
                   :season     "May - October"
                   :potency    :low-to-moderate}
   :PLATANUS      {:name-sr    "Platan"
                   :name-en    "Plane Tree"
                   :type       :tree
                   :season     "Late March - Mid May"
                   :potency    :moderate-to-high}
   :CUPRESS/TAXA. {:name-sr    "Tisa/Cempresi"
                   :name-en    "Yew/Juniper"
                   :type       :tree
                   :season     "February - Early April"
                   :potency    :moderate}
   :POPULUS       {:name-sr    "Topola"
                   :name-en    "Poplar/Aspen"
                   :type       :tree
                   :season     "March - April"
                   :potency    :low}
   :POACEAE       {:name-sr    "Trave"
                   :name-en    "Grass"
                   :type       :grass
                   :season     "April - September"
                   :potency    :very-high}
   :QUERCUS       {:name-sr    "Hrast"
                   :name-en    "Oak"
                   :type       :tree
                   :season     "April - June"
                   :potency    :moderate-to-high}
   :CELTIS        {:name-sr    "Koprovic"
                   :name-en    "Nettle Tree"
                   :type       :tree
                   :season     "March - Late May"
                   :potency    :low}})

(defn potency-label [potency]
  (case potency
    :very-high         "Very High"
    :high              "High"
    :moderate-to-high  "Moderate to High"
    :moderate          "Moderate"
    :low-to-moderate   "Low to Moderate"
    :low               "Low"
    :very-low          "Very Low"
    "Unknown"))

(defn type-label [type]
  (case type
    :tree  "Tree"
    :weed  "Weed"
    :grass "Grass"
    "Other"))

(defn allergens-by-type []
  (group-by (fn [[_ info]] (:type info)) allergen-info))