package com.viadeo.kasper.doc.element;

import com.google.common.base.Optional;

public enum DocumentedElementType {
      DOMAIN("domain", "domains")
    , COMMAND("command", "commands")
    , COMMAND_HANDLER("commandhandler", "commandhandlers")
    , QUERY("query", "queries")
    , QUERY_RESULT("queryresult", "queryresults")
    , QUERY_HANDLER("queryhandler", "queryhandlers")
    , EVENT("event", "events")
    , EVENT_LISTENER("eventlistener", "eventlisteners")
    , REPOSITORY("repository", "repositories")
    , CONCEPT("concept", "concepts")
    , RELATION("relation", "relations")
    ;

    protected final String type;
    protected final String pluralType;

    private DocumentedElementType(String type, String pluralType) {
        this.type = type;
        this.pluralType = pluralType;
    }

    public String getType() {
        return type;
    }

    public String getPluralType() {
        return pluralType;
    }

    public static Optional<DocumentedElementType> of(String type) {
        for (DocumentedElementType documentedElementType : DocumentedElementType.values()) {
            if (documentedElementType.type.equals(type) || documentedElementType.pluralType.equals(type)) {
                return Optional.of(documentedElementType);
            }
        }
        return Optional.absent();
    }
}
