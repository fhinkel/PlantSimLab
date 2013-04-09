# Be sure to restart your server when you modify this file.

# Your secret key for verifying cookie session data integrity.
# If you change this key, all old sessions will become invalid!
# Make sure the secret is at least 30 characters and all random, 
# no regular words or you'll be exposed to dictionary attacks.
ActionController::Base.session = {
  :key         => '_polynome_session',
  :secret      => 'fc7512c91b13ea18f12ce05199c5b81791aae24295f9691b81d81e73c73d07fe04cf13c042b686aca74ba6432fa2f0cb08f5a0762c0de538edb6d640ecde87a3'
}

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# ActionController::Base.session_store = :active_record_store
