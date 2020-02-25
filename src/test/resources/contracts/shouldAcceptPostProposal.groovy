import org.springframework.cloud.contract.spec.Contract
[
    Contract.make {
        name "should accept POST with new Proposal"
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
    },
    Contract.make {
        name "should return info for service"
        request{
            method 'GET'
            url '/info'

            headers {
                contentType('application/json')
            }
        }
        response {
            status OK()
            body("C4P v" + ((System.getenv("VERSION")!=null)?System.getenv("VERSION"):"0.0.0"))
        }
    }
]