
require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = "RNPaypal"
  s.version          = package['version']
  s.summary          = package['name']
  s.license          = package['license']
  s.description  = package['description']
  s.homepage     = "https://github.com/msasinowski/react-native-paypal-reborn"
  s.author             = "Maciej Sasinowski"
  s.platform     = :ios, "9.0"
  s.source       = { :git => 'https://github.com/msasinowski/react-native-paypal-reborn.git', :tag => s.version }
  s.source_files  = "ios/**/*.{h,m}"
  s.dependency "React"
  s.dependency "Braintree"
end
