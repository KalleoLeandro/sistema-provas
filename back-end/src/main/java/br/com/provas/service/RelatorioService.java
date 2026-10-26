package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.entity.Turma;
import br.com.provas.model.response.*;
import br.com.provas.repository.RelatorioRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

/** Consulta relatórios administrativos e do professor sem publicar notas para alunos. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class RelatorioService {
    private final RelatorioRepository repository;

    /** @param usuario identidade autenticada @return turmas disponíveis para relatórios */
    public List<Turma> turmas(LoginResponse usuario){
        /* Administradores consultam a escola; professores consultam seus vínculos ativos. */
        return repository.turmas(professor(usuario));
    }
    /** @param usuario identidade autenticada @param turma turma selecionada @return lista completa para impressão */
    public List<AlunoResponse> alunos(LoginResponse usuario,Long turma){
        /* Autoriza a turma antes de consultar a relação completa, sem truncar pela paginação da tela. */
        var professor=professor(usuario);validarTurma(professor,turma);
        return repository.alunos(professor,turma);
    }
    /** @param usuario identidade autenticada @param turma turma selecionada @return liberações autorizadas */
    public List<RelatorioProvaResponse> provas(LoginResponse usuario,Long turma){
        /* Uma turma compartilhada não permite consultar provas de outro professor. */
        var professor=professor(usuario);validarTurma(professor,turma);
        return repository.provas(professor,turma);
    }
    /** @param usuario identidade autenticada @param prova liberação selecionada @return resultados e ausências por aluno */
    public List<RelatorioProvaResponse.Resultado> resultados(LoginResponse usuario,Long prova){
        /* Exige autoria ou administração; mantém alunos sem tentativa e entregas históricas na relação. */
        var professor=professor(usuario);
        repository.prova(professor,prova).orElseThrow(()->new DefaultErrorException("Prova não encontrada.",HttpStatus.NOT_FOUND));
        return repository.resultados(professor,prova);
    }
    /** @param usuario identidade autenticada @return autor obrigatório para professor ou escopo administrativo */
    private Long professor(LoginResponse usuario){
        /* Recusa qualquer outro perfil mesmo quando a service for chamada fora do controller. */
        if("administrador".equals(usuario.perfil()))return null;
        if("professor".equals(usuario.perfil()))return usuario.id();
        throw new DefaultErrorException("Acesso não permitido aos relatórios.",HttpStatus.FORBIDDEN);
    }
    /** @param professor escopo do autor @param turma turma solicitada */
    private void validarTurma(Long professor,Long turma){
        /* Uma identificação fora do escopo não revela se a turma pertence a outro professor. */
        if(!repository.turmaPermitida(professor,turma))throw new DefaultErrorException("Turma não encontrada.",HttpStatus.NOT_FOUND);
    }
}
