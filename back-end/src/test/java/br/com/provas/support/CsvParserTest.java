package br.com.provas.support;
import br.com.provas.exceptions.DefaultErrorException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CsvParserTest {
    @Test void handlesBomSemicolonsEscapedQuotesAndPhysicalLines(){
        var rows=CsvParser.parse("\uFEFFnome;cgm\r\n\"Ana; \"\"Bia\"\"\";001\r\n\"Nome\ncompleto\";002\n");
        assertEquals(3,rows.size());assertEquals("Ana; \"Bia\"",rows.get(1).colunas().getFirst());
        assertEquals(3,rows.get(2).numero());assertEquals("Nome\ncompleto",rows.get(2).colunas().getFirst());
    }
    @Test void handlesCommaAndExcelSeparatorHint(){
        var rows=CsvParser.parse("sep=,\nnome,cgm\nAna,001\n");
        assertEquals(2,rows.size());assertEquals(3,rows.get(1).numero());assertEquals("001",rows.get(1).colunas().get(1));
    }
    @Test void rejectsUnclosedQuotesAndOversizedRowCount(){
        assertThrows(DefaultErrorException.class,()->CsvParser.parse("nome;cgm\n\"Ana;001"));
        assertThrows(DefaultErrorException.class,()->CsvParser.parse("nome;cgm\n"+"Ana;001\n".repeat(1001)));
    }
}
