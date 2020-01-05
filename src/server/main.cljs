(ns server.main
  (:require ["slack" :as slack]
            ["lodash" :as _]
            ))

(def value-a 2)

(defonce value-b 4)

(defn reload! []
  (println "Code updated!")
  (println "Trying values:" value-a value-b))

(def slack-token (.. js/process -env -SLACK_TOKEN))
(def icon-emoji (.. js/process -env -ICON_EMOJI))
(def leagueId (.. js/process -env -LEAGUE_ID))
(def seasonId (.. js/process -env -SEASON_ID))

(def bot (.client (.-rtm slack)))

(defn slackPost
  [msg outgoing]
  (.postMessage
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
      (.log js/console (str "🤖  bleep bloop: I responded with \"" txt "\"")))))

(defn main! []
  (do
    (.started
      bot
      (fn [payload]
        (set! (.-self bot) (.-self payload))
        (.log
          js/console
          (str "BOT: leagueid: " leagueId " seasonid: " seasonId ""))))
    (.message
      bot
      (fn [msg]
        (when (not (not (.-user msg)))
          (let [id (str "<@" (.. bot -self -id) ">")]
            (.log
              js/console
              "============================================================")
            (.log js/console "msg: " msg)
            (.log
              js/console
              "============================================================")
            (if (not (.includes _ (.match (.-text msg) #"(?im)<@([A-Z0-9])+>") id))
              (.log js/console "Error")
              (let [text (.-text msg)]
                (slackPost
                  msg
                  (str
                    (str "invalid request: " text)
                    ". Pass me a number. How about the current week of the season?"))))))))
    (.listen bot #js {:token slack-token})
    (println "App loaded!")
    ))
