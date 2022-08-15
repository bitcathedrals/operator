(ns operator.sms)

(import '(com.twilio.sdk TwilioRestClient TwilioRestException TwilioRestResponse))

(import '(com.twilio.sdk.resource.factory CallFactory SmsFactory))

(defn logon-main-account [account-sid auth-token]
  "logon-main-account SID AUTH

   logon to Twilio with SID and AUTH returning
   an Account object
  "
  (. (new TwilioRestClient account-sid auth-token) getAccount)
  )

(defn sms-message [ to from body ]
  {"To" to,
   "From" from,
   "Body" body} )

(defn sms-send [ account message ]
  "sms-send
   ACCOUNT SMS-MESSAGE

   send a MESSAGE using ACCOUNT. true is returned if the message is queued
   by twilio. false is returned otherwise.
  "
  (let [factory (. account getSmsFactory)
        status  (. factory create message)]

    (= "queued" (. status getStatus))
    ))
