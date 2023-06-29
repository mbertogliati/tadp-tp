import org.scalatest.matchers.should.Matchers._
import org.scalatest.freespec.AnyFreeSpec

import scala.util.{Failure, Success}
import Main._


class ProjectSpec extends AnyFreeSpec {


  "parsers basicos" - {

    "el parser anyChar" - {
      "tiene exito cuando la cadena empieza con cualquier caracter caracter" in {
        val resultadoParser = anyChar("hola")
        resultadoParser shouldBe a[Success[_]]
        resultadoParser.get shouldBe ResultadoParser('h', "ola")
      }
      "falla cuando la cadena esta vacía" in {
        val resultadoParser = anyChar("")
        resultadoParser shouldBe a[Failure[_]]
      }

    }

    "el parser char" - {
      "tiene exito cuando la cadena empieza con dado caracter" in {
        val resultadoParser = char('h')("hola")
        resultadoParser shouldBe a[Success[_]]
        resultadoParser.get shouldBe ResultadoParser('h', "ola")
      }
      "falla cuando la cadena esta vacía" in {
        val charH = char('h')
        charH("") shouldBe a[Failure[_]]
      }
    }

    "el parser void" - {
      "tiene exito cuando la cadena no es vacia" in {
        val resultadoParser = void("hola")
        resultadoParser shouldBe a[Success[_]]
        resultadoParser.get shouldBe ResultadoParser((), "hola")
      }
      "falla cuando la cadena es vacia" in {
        val resultadoParser = void("")
        resultadoParser shouldBe a[Failure[_]]
      }
    }

    "el parser letter" - {
      "tiene exito cuando la cadena empieza con una letra" in {
        val resultadoParser = letter("hola")
        resultadoParser shouldBe a[Success[_]]
        resultadoParser.get shouldBe ResultadoParser('h', "ola")
      }
      "falla cuando la cadena no empieza con una letra" in {
        val resultadoParser = letter("123")
        resultadoParser shouldBe a[Failure[_]]
      }
    }

    "el parser digit" - {
      "tiene exito cuando la cadena comienza con un número" in {
        val resultadoParser = digit("8as21as")
        resultadoParser shouldBe a[Success[_]]
        resultadoParser.get shouldBe ResultadoParser('8', "as21as")
      }
      "falla cuando la cadena no es un número" in {
        val resultadoParser = digit("asd")
        resultadoParser shouldBe a[Failure[_]]
      }
    }

    "el parser alphaNum" - {
      "tiene exito cuando la cadena comienza con un digito o letra" in {
        val resultadoParser1 = letter("hola")
        val resultadoParser2 = digit("8as21as")
        val resultadoParser3 = alphaNum("hola")
        val resultadoParser4 = alphaNum("8as21as")

        resultadoParser3 shouldBe resultadoParser1
        resultadoParser4 shouldBe resultadoParser2
      }
      "falla cuando la cadena no comienza con un digito o letra" in {
        val resultadoParser1 = letter("hola")
        val resultadoParser2 = digit("8as21as")
        val resultadoParser3 = alphaNum("-")

        resultadoParser3 shouldBe a[Failure[_]]
      }
    }

    "el parser string" - {
      "tiene exito cuando la cadena comienza con el string indicado" in {
        val resultadoParser = string("hola")("hola mundo!")
        resultadoParser shouldBe a[Success[_]]
        resultadoParser.get shouldBe ResultadoParser("hola", " mundo!")
      }
      "falla cuando la cadena no puede matchear el string indicado por completo" in {
        val resultadoParser = string("hola")("holgado")
        resultadoParser shouldBe a[Failure[_]]
      }
    }
  }

  "combinators" - {
    "el combinator OR" - {
      "crea un parser que combina dos parsers" in {
        val aob = char('a') <|> char('b')
        val resultadoParser1 = aob("arbol")
        val resultadoParser2 = aob("bellota")

        resultadoParser1 shouldBe a[Success[_]]
        resultadoParser1.get shouldBe ResultadoParser('a', "rbol")
        resultadoParser2 shouldBe a[Success[_]]
        resultadoParser2.get shouldBe ResultadoParser('b', "ellota")
      }
      "falla cuando no puede matchear con ningun parser" in {
        val aob = char('a') <|> char('b')
        val resultadoParser = aob("calle")

        resultadoParser shouldBe a[Failure[_]]
      }
    }

    "el combinator CONCAT" - {
      "crea un parser que aplica el primer parser y luego el segundo" in {
        val holaMundo = string("hola") <> string("mundo")
        val resultadoParser = holaMundo("holamundo!")

        resultadoParser shouldBe a[Success[_]]
        resultadoParser.get shouldBe ResultadoParser(("hola","mundo"), "!")
      }
      "falla cuando no puede matchear con ambos parsers a la vez" in {
        val holaMundo = string("hola") <> string("mundo")
        val resultadoParser = holaMundo("holam!")

        resultadoParser shouldBe a[Failure[_]]
      }
    }

    "el combinator RIGHTMOST" - {
      "crea un parser que aplica dos parsers secuencialmente y retorna el resultado del segundo" in {
        val holaMundo = string("hola") ~> string("mundo")
        val resultadoParser = holaMundo("holamundo!")

        resultadoParser shouldBe a[Success[_]]
        resultadoParser.get shouldBe ResultadoParser("mundo", "!")
      }
    }

    "el combinator LEFTMOST" - {
      "crea un parser que aplica dos parsers secuencialmente y retorna el resultado del primero" in {
        val holaMundo = string("hola") <~ string("mundo")
        val resultadoParser = holaMundo("holamundo!")

        resultadoParser shouldBe a[Success[_]]
        resultadoParser.get shouldBe ResultadoParser("hola", "!")
      }
    }
  }

  "operaciones" - {
    "satisfies" - {
      "crea un parser que funciona sólo si el parser base funciona y además el elemento parseado cumple esa condición" in {
        val holaMundo = string("hola").satisfies(s => s.size == 4)
        val resultadoParser = holaMundo("hola")

        resultadoParser shouldBe a[Success[_]]
        resultadoParser.get shouldBe ResultadoParser("hola", "")
      }
      "falla cuando no se cumple la condicion" in {
        val holaMundo = string("hola").satisfies(s => s.size == 5)
        val resultadoParser = holaMundo("hola")

        resultadoParser shouldBe a[Failure[_]]
      }
    }

    "opt" - {
      "funciona cuando funciona el parser original" in {
        val hola = string("hola")
        val resultadoParser = hola("hola!")

        resultadoParser shouldBe a[Success[_]]
        resultadoParser.get shouldBe ResultadoParser("hola", "!")
      }
      "funciona aunque no funcione el parser original pero no consume caracteres" in {
        val hola = string("hola").opt
        val resultadoParser = hola("123")

        resultadoParser shouldBe a[Success[_]]
        resultadoParser.get shouldBe ResultadoParser(None, "123")
      }
    }

    "*" - {
      "aplica el parser todas las veces posibles" in {
        val hola = string("hola").*
        val resultadoParser1 = hola("holaholahola!")
        val resultadoParser2 = hola("!")

        resultadoParser1 shouldBe a[Success[_]]
        resultadoParser1.get shouldBe ResultadoParser(List("hola","hola","hola"), "!")
        resultadoParser2 shouldBe a[Success[_]]
        resultadoParser2.get shouldBe ResultadoParser(List(), "!")
      }
    }

    "+" - {
      "aplica el parser al menos una vez" in {
        val hola = string("hola").+
        val resultadoParser = hola("holaholahola!")

        resultadoParser shouldBe a[Success[_]]
        resultadoParser.get shouldBe ResultadoParser(List("hola", "hola", "hola"), "!")
      }
      "falla si no puede aplicar el parser al menos una vez" in {
        val hola = string("hola").+
        val resultadoParser = hola("!")

        resultadoParser shouldBe a[Failure[_]]
      }
    }

    "sepBy" - {
      "parsea n (> 1) veces el parser de contenido y entre medio aplica el parser separador." in {
        val hola = digit.sepBy(char('-'))
        val resultadoParser = hola("12-34-56!")

        resultadoParser shouldBe a[Success[_]]
        resultadoParser.get shouldBe ResultadoParser(List(List('1','2'),List('3','4'),List('5','6')), "!")
      }
      "falla si no matchea el parser de contenido" in {
        val hola = digit.sepBy(char('-'))
        val resultadoParser = hola("abc!")

        resultadoParser shouldBe a[Failure[_]]
      }
      "falla si no matchea el separador" in {
        val hola = digit.sepBy(char('-'))
        val resultadoParser = hola("123 456!")
        // TODO: no falla como deberia
        resultadoParser shouldBe a[Failure[_]]
      }
    }

    "const" - {
      "devuelve como elemento parseado un valor constante" in {
        val trueParser = string("true").const(true)
        val resultadoParser = trueParser("true!")

        resultadoParser shouldBe a[Success[_]]
        resultadoParser.get shouldBe ResultadoParser(true, "!")
      }
      "falla si falla el parser original" in {
        val trueParser = string("true").const(true)
        val resultadoParser = trueParser("tru!")

        resultadoParser shouldBe a[Failure[_]]
      }
    }

    "map" - {
      "convierte el valor parseado utilizando la funcion recibida" in {
        case class Persona(nombre: String, apellido: String)
        val personaParser = (alphaNum.* <> (char(' ') ~> alphaNum.*))
          .map { case (nombre, apellido) => Persona(nombre, apellido) }
        // TODO: esta bien que alphaNum.* arme List[Char] en vez de String?
        val resultadoParser = personaParser("Lionel Messi!")

        resultadoParser shouldBe a[Success[_]]
        resultadoParser.get shouldBe ResultadoParser(List("Lionel","Messi"), "!")
      }
    }
  }
}

