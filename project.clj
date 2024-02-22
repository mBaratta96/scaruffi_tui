(defproject scaruffi_tui "0.1.0-SNAPSHOT"
  :description "Get Scaruffi's History of Music in your termial"
  :url "https://github.com/mBaratta96/scaruffi_tui"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0",
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [hickory "0.7.1"]
                 [org.clj-commons/clj-http-lite "1.0.13"]
                 [org.clj-commons/pretty "2.2.1"]]
  :injections [(.. System (setProperty "clj-commons.ansi.enabled" "true"))]
  :main scaruffi-tui.core
  :target-path "target/%s"
  :aliases {"native"
            ["shell"
             "native-image"
             "--report-unsupported-elements-at-runtime"
             "--initialize-at-build-time"
             "--no-server"
             "-jar"
             "./target/uberjar/scaruffi_tui-0.1.0-SNAPSHOT-standalone.jar"
             "-H:Name=./target/scaruffi"
             "--diagnostics-mode"
             "--enable-url-protocols=https"]}
  :profiles {:uberjar {:aot :all}, :dev {:plugins [[lein-shell "0.5.0"]]}})
