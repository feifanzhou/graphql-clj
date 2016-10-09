(ns graphql-clj.validator.rules.default-values-of-correct-type
  "A GraphQL document is only valid if all variable default values are of the type expected by their definition."
  (:require [clojure.spec :as s]
            [graphql-clj.validator.errors :as e]
            [zip.visit :as zv]))

(defn- default-for-required-arg-error [var-name type]
  (format "Variable '$%s' of type '%s!' is required and will never use the default value. Perhaps you meant to use type '%s'."
          var-name type type))

(zv/defvisitor default-for-required-field :pre [{:keys [node-type required default-value variable-name type-name] :as n} s]
  (case node-type
    :variable-definition
    (when (and required default-value)
      {:state (e/update-errors s (default-for-required-arg-error variable-name type-name))})
    nil))

(defn- bad-value-for-default-error [var-name type default-value]
  (format "Variable '$%s' of type '%s' has invalid default value: \"%s\". Reason: %s value expected."
          var-name type default-value type))

(zv/defvisitor bad-value-for-default :pre [{:keys [node-type spec variable-name default-value type-name] :as n} s]
  (case node-type
    :variable-definition
    (when (and spec default-value (not (s/valid? spec default-value)))
      {:state (e/update-errors s (bad-value-for-default-error variable-name type-name default-value))})
    nil))

(def rules
  [default-for-required-field
   bad-value-for-default])
