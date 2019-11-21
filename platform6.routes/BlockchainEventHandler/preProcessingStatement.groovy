import com.fasterxml.jackson.databind.ObjectMapper
import org.web3j.protocol.core.methods.response.Log

class LogToJSON {
    def String toJSON(Log log) {
        return new ObjectMapper().writeValueAsString(log);
    }
}

p6.camel.registerBean("logToJSON", LogToJSON)

def ethClientURL = p6.appconfig.get('ethClientURL')
def contractAddress =  p6.appconfig.get('contractAddress')
