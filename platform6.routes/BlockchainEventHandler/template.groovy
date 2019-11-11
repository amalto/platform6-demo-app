// -------------------------------------------------------------------------------------------
// Define routes with pre-processing instructions (ie. to define and register beans).
//
// Parameters:
//    preProcessingStatement:   Pre-processing instructions
//    addRoutes:                The route definitions
//
// Example:
//    p6.camel.registerBean("myBean", ServiceBean )
//
// More information on https://documentation.amalto.com/platform6/latest/develop-app/built-in-services/routes/routes-dsl/#groovy-bean-execution
// -------------------------------------------------------------------------------------------


${preProcessingStatement}

p6.camel.getCtx().addRoutes(new RouteBuilder() {

    void configure() {

        ${addRoutes}

    }
})
