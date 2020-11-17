(ns com.uofantarctica.chaosmonkey.server.main
  (:require ["slack" :as slack]
            ["lodash" :as _]
            [clojure.string :as str]))

(def slack-token (.. js/process -env -SLACK_TOKEN))
(def icon-emoji (.. js/process -env -ICON_EMOJI))
(def leagueId (.. js/process -env -LEAGUE_ID))
(def seasonId (.. js/process -env -SEASON_ID))

(defmacro println [& args]
  `(.log js/console ~@args))

(defn post
  [msg outgoing]
  (comment .postMessage
    (.-chat slack)
    #js
    {:token slack-token,
     :icon_emoji icon-emoji,
     :channel (.-channel msg),
     :username "C ⚛ H ⛾ A ☭ O ⛐ S  ☉ M ☲ O ☬ N ⚰ K ♛ E ⛤ Y",
     :text (str "" (.stringify js/JSON outgoing) "")}
    (fn [err data]
      (when err (throw err))
      (def txt (.. data -message -text))
      (.log js/console (str "🤖  bleep bloop: I responded with \"" txt "\""))))
  (println "🤖  bleep bloop: I responded with: " outgoing)
  )

(defn extract-message-text [text bot-id]
  "Extract the bot name out of message, return the message or nil if
  it is empty."
  (println "text contains bot id: " text ", with id: " bot-id "includes?: "
           (str/index-of text bot-id))
  (when (contains? text bot-id)
    (let [message (str/trim (str/replace text bot-id ""))]
      (when (not (empty? message))
        message))))

(defn ff-api [slack-msg command-tokens]
  (if (or (empty? command-tokens) (not (int? (js/parseInt (first command-tokens)))))
    (post slack-msg ". Pass me a number. How about the current week of the season?")
    ))

(defn handle-message [slack-msg text]
  (let [tokens (str/split text " ")
        dispatching-token (first tokens)
        command-tokens (rest tokens)]
    (cond
      (= "ff" dispatching-token) (ff-api slack-msg command-tokens)
      :else (post slack-msg "No known subcommand provided. Try typing ff to me."))))

(defn on-connect [bot]
  (.started
    bot
    (fn [payload]
      (set! (.-self bot) (.-self payload))
      (def bot-id (str "<@" (.. bot -self -id) ">"))
      (println (str "BOT: leagueid: " leagueId " seasonid: " seasonId "."))))
  (.listen bot #js {:token slack-token}))

(defn on-message [bot]
  (.message
    bot
    (fn [^js msg]
      (when (not (not (.. msg -user)))
        (try
          (->
            (let [text (extract-message-text bot-id (.-text msg))]
              (println "PRINT " text " BOOL EXT" (boolean text))
            (if text
              (println "TRUE")
              (println "FLASE")
              )))
          (catch js/Error ex (println "Error: " (.-stack ex))))))))

(defn start-slack-connection [chat]
  (let [bot (.client (.-rtm slack))]
    (on-connect bot)
    (on-message bot)
    (println "App loaded!")))

(defn main! []
  (start-slack-connection slack))

(defn reload! []
  (start-slack-connection slack)
  (println "Code updated!"))
