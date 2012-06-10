import scala.util.parsing.combinator._  
import scala.util.matching.Regex  
import scala.util.parsing.input.CharSequenceReader

case class Command(action: String, properties: List[Property], name: String)
case class Property(name: String, value: String)

object SToolVirt {

    import LibvirtWrapper._

    class Terminal() extends RegexParsers {

        private var exit = false;

        def start() = {
            while(!exit) {
                Console.print("$ ")
                val line = Console.readLine()
                if (line.equals("Q")) 
                    exit = true
                else
                    scan(line)
            }
        }

        def scan(str: String) = {            
            val command = DefaultParser(str).get
            chooseActions(command)
        }

        def chooseActions(command: Command) = command.action match {
            case "create"   => LibvirtWrapper.createAction(command.properties, command.name)
            case "destroy"  => LibvirtWrapper.destroyAction(command.name)
            case "start"    => LibvirtWrapper.startAction(command.name)
            case "stop"     => LibvirtWrapper.stopAction(command.name)
            case "ping"     => LibvirtWrapper.pingAction(command.name)
            case "info"     => LibvirtWrapper.infoAction(command.name)
            case _          => Console.println("error")
        }
    }    
}

object DefaultParser extends JavaTokenParsers {

    def apply(input: String) = parseAll(command, input)

    def command = action ~ properties ~ message ^^ {
        case a ~ p ~ m => new Command(a, p, m)
    }

    val action = ident

    val message = """[\w\d-_]+""".r

    val properties = rep(property)

    val property = "-" ~ propertyName ~ propertyValue ^^ {
        case "-" ~ n ~ v => new Property(n, v)
    }

    val propertyName: Parser[String] = ident

    val propertyValue: Parser[String] = ident
}

