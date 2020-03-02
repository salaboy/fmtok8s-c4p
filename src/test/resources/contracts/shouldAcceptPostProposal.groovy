import org.springframework.cloud.contract.spec.Contract
[
    Contract.make {
        name "should accept POST with new Proposal"
        request{
            method 'POST'
            url '/'
            body([
                "title": $(anyNonEmptyString()),
                "description": $(anyNonEmptyString()),
                "author": $(anyNonEmptyString()),
                "email": $(anyEmail())
            ])
            headers {
                contentType('application/json')
            }
        }
        response {
            status OK()
            headers {
                contentType('application/json')
            }
            body(
                    "id": $(anyUuid()),
                    "title": $(anyNonEmptyString()),
                    "description": $(anyNonEmptyString()),
                    "author": $(anyNonEmptyString()),
                    "email": $(anyEmail())
            )
        }
    }

]