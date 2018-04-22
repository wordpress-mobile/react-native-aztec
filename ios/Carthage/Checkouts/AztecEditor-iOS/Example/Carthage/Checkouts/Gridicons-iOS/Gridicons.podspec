Pod::Spec.new do |s|
  s.name         = "Gridicons"
  s.version      = "0.8"
  s.summary      = "Gridicons is a tiny framework which generates Gridicon images at any resolution."

  s.homepage     = "http://apps.wordpress.com"
  s.license      = { :type => "GPLv2" }
  s.author       = { "James Frost" => "james.frost@automattic.com" }
  s.social_media_url   = "http://twitter.com/WordPressiOS"

  s.platform     = :ios, "9.0"
  s.source       = { :git => "https://github.com/Automattic/Gridicons-iOS.git", :tag => s.version.to_s }

  s.source_files  = "Gridicons/Gridicons/**/*.swift"
  s.requires_arc = true
end
