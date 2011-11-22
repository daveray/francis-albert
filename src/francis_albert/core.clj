;  Copyright (c) Dave Ray, 2011. All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this 
;   distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns francis-albert.core
  (:use [overtone.live :exclude [select timer]]
        [seesaw.core])
  (:require [seesaw.bind :as b]))

; An instrument to play our notes. 60 is middle C. 
(definst beep [note 60 vol 0.5]
  (let [freq (midicps note)
        src (sin-osc freq)
        env (env-gen (perc 0.3 2) :action FREE)]
    (* vol src env)))

; Helper to play a chord. TODO: Is this hidden in overtone somewhere?
(defn play-chord [s key off type]
  (play s (map #(+ key 60 off %) (resolve-chord type))))

(def notes [:1 :#1 :2 :#2 :3 :4 :#4 :5 :#5 :6 :#6 :7])
(def note-indices (into {} (map-indexed #(vector %2 %1) notes)))

; The game state
(def state (atom
  { :expected :1
    :key 0
    :new-question? true
    :total-guesses 0
    :correct-guesses 0 }))

; Play a single example. Just a ii-V7-I cadence followed by the expected note
(defn play-example [state]
  (let [{:keys [key expected]} state
        t (now)]
    (at (+ t 0)    (play-chord beep key 2 :minor7))
    (at (+ t 1200) (play-chord beep key 7 :dom7))
    (at (+ t 2400) (play-chord beep key 0 :major))
    (at (+ t 3800) (beep (+ key (note-indices expected) 60)))))

; Pick a random note
(defn choose-note [] (notes (rand-int (count notes))))

; State transition function given state and guessed note
(defn make-guess [state guess]
  (if (= guess (:expected state))
    (-> state
      (update-in [:correct-guesses] inc)
      (update-in [:total-guesses] inc)
      (assoc :expected (choose-note)
             :key (rand-int 12)
             :new-question? true))
    (-> state
      (update-in [:total-guesses] inc)
      (assoc :new-question? false))))


; Build up the structure of the UI
(defn make-ui [on-close]
  (frame
    :on-close on-close
    :title "Francis Albert"
    :size [640 :by 480]
    :content (border-panel
               :border 5 :hgap 5 :vgap 5
               :north "Listen to the cadence and the note that follows it. Then guess the interval of the note. Good luck!"
               :center (grid-panel 
                         :columns 6
                         :items (map #(button :id % :class :guess :text (name %)) notes))
               :south (grid-panel
                        :columns 3
                        :items [(label :id :score :text "Current Score:")
                                (label :id :indicator 
                                       :halign :center
                                       :text ""
                                       :opaque? true)
                                (button :id :replay :text "Listen Again")]))))

; set up listeners and stuff
(defn add-behaviors [root]
  (listen 
    (select root [:.guess])
    :action
    (fn [e]
      (let [{:keys [new-question?] :as next-state} (swap! state make-guess (id-of e))]
        (when new-question?
          (play-example next-state)))))

  (listen 
    (select root [:#replay])
    :action
    (fn [e]
      (play-example @state)))

  (b/bind
    state
    (b/tee
      (b/bind 
        (b/transform
          (fn [s] (format "Current score: %d of %d correct" 
                          (:correct-guesses s) 
                          (:total-guesses s))))
        (b/property (select root [:#score]) :text))

      (b/bind
        (b/transform
          (fn [s] (if (:new-question? s) :green :red)))
        (b/property (select root [:#indicator]) :background))

      (b/bind
        (b/transform
          (fn [s] (if (:new-question? s) "That's Right!" "Try Again :(")))
        (b/property (select root [:#indicator]) :text))))

  root)

(defn app [on-close]
  (invoke-later 
    (-> (make-ui on-close)
      add-behaviors
      show!)
    (play-example @state)))

(defn -main [& args]
  (app :exit))

;(app :dispose)

