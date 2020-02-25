import org.springframework.cloud.contract.spec.Contract
Contract.make {
    description "should accept POST with new Proposal"
    request{
        method 'POST'
        url '/'
        body([
            "title": "",
            "description": "",
            "author": "",
            "email": ""
        ])
        headers {
            contentType('application/json')
        }
    }
    response {

        status OK()

    }
}