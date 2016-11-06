(ns lymchat.externs
  (:require [cljs.compiler.api :as compiler]
            [cljs.analyzer.api :as analyzer]
            [cljs.analyzer :as ana]
            [clojure.walk :refer [prewalk]]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [cljs.closure :as closure]
            [cljs.env :as env]
            [clojure.tools.reader :as r]
            [clojure.tools.reader.reader-types :refer [string-push-back-reader]]
            [cljs.tagged-literals :as tags])
  (:import (clojure.lang LineNumberingPushbackReader)))

(defonce cenv (analyzer/empty-state))
(comment
  (analyzer/with-state cenv
   (let [src (io/file "src/lymchat/core.cljs")]
     (closure/compile src
                      {:output-file (closure/src-file->target-file src)
                       :force       true
                       :mode        :interactive}))))
(defn get-namespaces
  []
  (:cljs.analyzer/namespaces @cenv))

(defn print-ast [ast]
  (pprint  ;; pprint indents output nicely
   (prewalk ;; rewrite each node of the ast
    (fn [x]
      (if (map? x)
        (select-keys x [:children :name :form :op]) ;; return selected entries of each map node
        x))  ;; non-map nodes are left unchanged
    ast)))

(defn read-file
  [filename]
  (try
    (let [reader (string-push-back-reader (slurp filename))
          endof (gensym)]
      (binding [r/*read-eval* false
                r/*data-readers* tags/*cljs-data-readers*]
        (->> #(r/read reader false endof)
             (repeatedly)
             (take-while #(not= % endof))
             (doall))))
    (catch Exception e
      (println e)
      '())))

(defn file-ast
  "Return the ClojureScript AST for the contents of filename. Tends to
  be large and to contain cycles -- be careful printing at the REPL."
  [filename]
  (binding [ana/*cljs-ns* 'cljs.user ;; default namespace
            ana/*cljs-file* filename]
    (mapv
     (fn [form]
       (try (ana/no-warn (ana/analyze (ana/empty-env) form nil))
            (catch Exception e
              (prn filename e))))
     (read-file filename))))

(defn flatten-ast [ast]
  (mapcat #(tree-seq :children :children %) ast))

;; (def flat-ast (flatten-ast (file-ast "src/lymchat/shared/ui.cljs")))
;; (count flat-ast)
(defn get-interop-used
  "Return a set of symbols representing the method and field names
  used in interop forms in the given sequence of AST nodes."
  [flat-ast]
  (set (keep #(let [ret (and (map? %)
                             (when-let [sym (some % [:method :field])]
                               (when-not (str/starts-with? sym "cljs")
                                 sym)))]
                (if ret
                  ret
                  nil)) flat-ast)))

(defn externs-for-interop [syms]
  (apply str
    "var DummyClass={};\n"
    (map #(str "DummyClass." % "=function(){};\n")
      syms)))

(defn var-defined?
  "Returns true if the given fully-qualified symbol is known by the
  ClojureScript compiler to have been defined, based on its mutable set
  of namespaces."
  [sym]
  (contains? (:defs (get (get-namespaces) (symbol (namespace sym))))
             (symbol (name sym))))

(defn get-vars-used
  "Return a set of symbols representing all vars used or referenced in
  the given sequence of AST nodes."
  [requires flat-ast]
  (->> flat-ast
       (filter #(let [ns (-> % :info :ns)]
                  (and (= (:op %) :var)
                       ns
                       (not= ns 'js))))
       (map #(let [sym (-> % :info :name)
                   sym-namespace (get requires (symbol (namespace sym)))
                   sym-name (name sym)]
               (if sym-namespace
                 (symbol (str sym-namespace) sym-name)
                 sym)))))

(defn extern-for-var [sym]
  (if (= "js" (namespace sym))
    (format "var %s={};\n" (name sym))
    (format "var %s={};\n%s.%s={};\n"
            (namespace sym) (namespace sym) (name sym))))

(defn externs-for-vars [syms]
  (apply str (map extern-for-var syms)))

(defn get-undefined-vars [requires flat-ast]
  (->> (get-vars-used requires flat-ast)
       (remove var-defined?)))

(defn get-undefined-vars-and-interop-used [file]
  (let [ast (file-ast file)
        ns-name (:name (first ast))
        ns-requires (:requires (first ast))
        flat-ast (flatten-ast ast)]
    [(get-undefined-vars ns-requires flat-ast)
     (get-interop-used flat-ast)]))

(defn cljs-file?
  "Returns true if the java.io.File represents a normal Clojurescript source
  file."
  [^java.io.File file]
  (and (.isFile file)
       (.endsWith (.getName file) ".cljs")))

(defn project-externs
  "Generate an externs file"
  [project & [build-type outfile]]
  (let [source-paths ["src" "env/prod"]
        files        (->> source-paths
                          (map #(str "/Users/tienson/codes/exponent/lymchat" "/" %))
                          (map io/file)
                          (mapcat file-seq)
                          (filter cljs-file?))

        col          (apply concat (doall (map get-undefined-vars-and-interop-used files)))
        vars (-> (flatten (take-nth 2 col))
                 (externs-for-vars))
        interop (-> (flatten (take-nth 2 (rest col)))
                    (externs-for-interop))]
    (str vars interop)))

(comment
  (def path "src/lymchat/shared/ui.cljs")
  (project-externs nil))
