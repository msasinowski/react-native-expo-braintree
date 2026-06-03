require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "ExpoBraintree"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => min_ios_version_supported }
  s.source       = { :git => "https://github.com/maciejsasinowski/react-native-expo-braintree.git", :tag => "#{s.version}" }


  s.source_files = [
    "ios/**/*.{swift}",
    "ios/**/*.{m,mm}",
    "cpp/**/*.{hpp,cpp}",
  ]

  s.dependency "Braintree/Core", "7.5.0"
  s.dependency "Braintree/Card", "7.5.0"
  s.dependency "Braintree/ThreeDSecure", "7.5.0"
  s.dependency "Braintree/PayPal", "7.5.0"
  s.dependency "Braintree/Venmo", "7.5.0"
  s.dependency "Braintree/DataCollector", "7.5.0"

  s.dependency 'React-jsi'
  s.dependency 'React-callinvoker'

  load 'nitrogen/generated/ios/ExpoBraintree+autolinking.rb'
  add_nitrogen_files(s)

  install_modules_dependencies(s)
end
