;  Copyright (c) Dave Ray, 2011. All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this
;   distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns francis-albert.core
  (:use [overtone.live :exclude [config select timer]]
        [seesaw.core])
  (:require [seesaw.bind :as b]
            [seesaw.dev :as dev]
            [overtone.music.pitch :as pitch]))

; For sanity
(dev/debug!)

; An instrument to play our notes. 60 is middle C.
(definst beep [note 60 vol 0.5]
  (let [freq (midicps note)
        src (sin-osc freq)
        env (env-gen (perc 0.3 2) :action FREE)]
    (* vol src env)))

(defn play [inst notes]
  (doseq [n notes]
    (inst n)))

; Helper to play a chord. TODO: Is this hidden in overtone somewhere?
(defn play-note [s key off]
  (play s [(+ key 60 off)]))

(defn play-chord [s key off type]
  (play s (map #(+ key 60 off %) (resolve-chord type))))

(def notes [:i :i# :ii :ii# :iii :iv :iv# :v :v# :vi :vi# :vii])
(def note-info
  {
    :i   {:index 0  :answer []}
    :i#  {:index 1  :answer [:i]}
    :ii  {:index 2  :answer [:i]}
    :ii# {:index 3  :answer [:ii :i]}
    :iii {:index 4  :answer [:ii :i]}
    :iv  {:index 5  :answer [:iii :ii :i]}
    :iv# {:index 6  :answer [:v :vi :vii :I]}
    :v   {:index 7  :answer [:vi :vii :I]}
    :v#  {:index 8  :answer [:vi :vii :I]}
    :vi  {:index 9  :answer [:vii :I]}
    :vi# {:index 10 :answer [:vii :I]}
    :vii {:index 11 :answer [:I]}
    :I   {:index 12}
   })

;(:m7-9 :m+5 :dim7 :diminished :m6*9 :m7-5 :a :6 :7+5 :11+ :M7 :7 :9+5 :5 :minor :dim :dom7 :9sus4 :1 :m9+5 :sus2 :m9 :m7 :+5 :m7+5-9 :m :m7+5 :i :i7 :sus4 :m6 :7sus4 :9 :major7 :major :7-5 :6*9 :7sus2 :maj11 :7+5-9 :m11 :M :11 :7-9 :maj9 :minor7 :7-10 :13 :m13 :augmented :diminished7 :m11+)

(def chord-qualities [:major :minor :dim :augmented])

; The game state
(def state (atom
  { :mode :intervals
    :expected :i
    :inst beep
    :key 0
    :new-question? true
    :total-guesses 0
    :correct-guesses 0 }))

(defmulti play-example :mode)
(defmulti play-answer :mode)
(defmulti choose-next-question identity)

;; Functional Interval Mode
; Play a single example. Just a ii-V7-I cadence followed by the expected note
(defmethod play-example :intervals [{:keys [inst key expected]}]
  (let [t (+ (now) 100)]
    (at (+ t 0)  (play-chord inst key 2 :minor7))
    (at (+ t 1200) (play-chord inst key 7 :dom7))
    (at (+ t 2400) (play-chord inst key 0 :major))
    (at (+ t 3800) (inst (+ key (get-in note-info [expected :index]) 60)))))

(defmethod play-answer :intervals [{:keys [inst key expected]}]
  (let [t            (+ (now) 100)
        notes        (cons expected (get-in note-info [expected :answer]))
        note-indexes (map #(get-in note-info [% :index]) notes)]
    (doseq [[i n] (map-indexed vector note-indexes)]
      (at (+ t (* i 500)) (play-note inst key n)))))

(defmethod choose-next-question :intervals [_]
  (rand-nth notes))

;; Chord quality mode

(defmethod play-example :chord-qualities [{:keys [inst key expected]}]
  (at (+ (now) 100)
      (play-chord inst key 0 expected)))

(defmethod play-answer :chord-qualities [_])

(defmethod choose-next-question :chord-qualities [_]
  (rand-nth chord-qualities))

;; General State Transitions

(defn switch-mode [state new-mode]
  (println "switch-mode " new-mode)
  (assoc state :mode new-mode
               :expected (choose-next-question new-mode)
               :new-question? true))

(defn make-guess [state guess]
  (if (= guess (:expected state))
    (-> state
      (update-in [:correct-guesses] inc)
      (update-in [:total-guesses] inc)
      (assoc :expected (choose-next-question (:mode state))
             :key (rand-int 12)
             :new-question? true))
    (-> state
      (update-in [:total-guesses] inc)
      (assoc :new-question? false))))


(defn make-interval-panel
  []
  (border-panel
    :id :intervals
    :border 5 :hgap 5 :vgap 5
    :north "Listen to the cadence and the note that follows it. Then guess the interval of the note. Good luck!"
    :center (grid-panel
              :columns 6
              :items (map #(button :id % :class :guess :text (name %)) notes))))

(defn make-chord-quality-panel
  []
  (border-panel
    :id :chord-qualities
    :border 5 :hgap 5 :vgap 5
    :north "Listen to the chord and identify its quality"
    :center (grid-panel
              :columns 2
              :items (map #(button :id % :class :guess :text (name %)) chord-qualities))))

; Build up the structure of the UI
(defn make-ui [on-close]
  (frame
    :on-close on-close
    :title "Francis Albert"
    :size [640 :by 480]
    :content (border-panel
               :border 5 :hgap 5 :vgap 5
               :center (tabbed-panel
                         :id   :tabs
                         :tabs [{:title "Functional Intervals" :content (make-interval-panel)}
                                {:title "Chord Qualities" :content (make-chord-quality-panel)}])
               :south (grid-panel
                        :columns 2
                        :items [(label :id :score :text "Current Score:")
                                (label
                                  :id :indicator
                                  :halign :center
                                  :text ""
                                  :paint { :before (fn [c g]
                                                    (.setOpaque c false)
                                                    (.setColor g (.getBackground c))
                                                    (.fillRoundRect g 0 0 (width c) (height c) 20 20))})
                                (button :id :replay :text "Listen Again")
                                (button :id :answer :text "Listen To Answer")]))))

; set up listeners and stuff
(defn add-behaviors [root]

  (listen
    (select root [:#tabs])
    :selection
    (fn [e]
      (println (id-of (:content (selection e))))
      (swap! state switch-mode (-> (selection e) :content id-of))
      (stop)
      (play-example @state)))

  (listen
    (select root [:.guess])
    :action
    (fn [e]
      (let [{:keys [new-question?]} (swap! state make-guess (id-of e))]
        (when new-question?
          (play-example @state)))))

  (listen
    (select root [:#replay])
    :action
    (fn [e]
      (play-example @state)))

  (listen
    (select root [:#answer])
    :action
    (fn [e]
      (play-answer @state)))

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

