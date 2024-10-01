package com.oppo.api.Opportunity.API.Services.EscolasService;

import com.oppo.api.Opportunity.API.DTOs.EscolasDTOs.EscolasDTO;
import com.oppo.api.Opportunity.API.Models.Escolas.EscolasEntity;
import com.oppo.api.Opportunity.API.Repositories.EscolasRespository.EscolasRepository;
import com.oppo.api.Opportunity.API.SecurityPaths.Auth.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class EscolaServicesImpl implements EscolaServices {
    @Autowired
    private final AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    public EscolaServicesImpl(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }


    @Autowired
    private EscolasRepository escolasRepository;



    @Override
    public ResponseEntity<?> create(EscolasDTO escolasDTO) {
        try {
            // Verifica se a escola já existe pelo CNPJ
            Optional<EscolasEntity> encontrarEscola = escolasRepository.findByCNPJ(escolasDTO.cnpj());
            if (encontrarEscola.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("CNPJ já registrado. Não é possível criar uma escola com este CNPJ");
            }

            // Cria uma nova entidade de escola a partir do DTO
            EscolasEntity escola = new EscolasEntity();
            escola.setNome(escolasDTO.nome());
            escola.setCNPJ(escolasDTO.cnpj());
            escola.setSenha(escolasDTO.senha());
            escola.setEndereco(escolasDTO.endereco());
            escola.setDiretor(escolasDTO.diretor());

            // Salva a nova escola no repositório
            EscolasEntity savedEscola = escolasRepository.save(escola);
            return ResponseEntity.status(HttpStatus.CREATED).body("Escola criada com sucesso! ID: " + savedEscola.getId());

        } catch (Exception e) {
            // Aqui você pode fazer um log do erro para depuração
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar a escola: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> login(EscolasDTO escolasDTO) {
        try {
            // Verifica se a escola com o CNPJ fornecido existe
            Optional<EscolasEntity> optEscola = escolasRepository.findByCNPJ(escolasDTO.cnpj());

            // Verifica se a escola foi encontrada e se a senha está correta
            if (optEscola.isPresent() && optEscola.get().getSenha().equals(escolasDTO.senha())) {
                // Autentica o usuário
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(escolasDTO.cnpj(), escolasDTO.senha())
                );

                // Gera o token JWT
                String token = jwtUtil.createToken(escolasDTO);

                // Retorna a escola e o token no corpo da resposta
                return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                        "message", "Login bem-sucedido",
                        "escola", optEscola.get(),
                        "token", token
                ));
            }

            // Retorna erro caso o CNPJ ou a senha estejam errados
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CNPJ ou senha estão incorretos, ou o usuário não existe.");

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Credenciais inválidas: CNPJ ou senha estão incorretos.");
        } catch (Exception e) {
            // Lida com qualquer outro tipo de erro inesperado
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao tentar iniciar a sessão. "+e);
        }
    }


    @Override
    public ResponseEntity<?> update(EscolasDTO escolasDTO) {
        try{
            Optional<EscolasEntity> optEscola = escolasRepository.findByCNPJ(escolasDTO.cnpj());
            if (optEscola.isPresent()){
               EscolasEntity escola = optEscola.get();
               escola.setNome(escolasDTO.nome());
               escola.setCNPJ(escolasDTO.cnpj());
               escola.setEndereco(escolasDTO.endereco());
               escola.setSenha(escolasDTO.senha());
               escola.setRole(escolasDTO.role());
               return ResponseEntity.status(HttpStatus.OK).body(escolasRepository.save(escola));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Escola não encontrada.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao atualizar escola.");

        }
    }

    @Override
    public ResponseEntity<?> addAluno(EscolasDTO escolasDTO) {
        try {
            Optional<EscolasEntity> optEscola = escolasRepository.findByCNPJ(escolasDTO.cnpj());
            if (optEscola.isPresent()){
                EscolasEntity escola = optEscola.get();
                return  ResponseEntity.status(HttpStatus.OK).body("Aluno adicionado: "+escola.getAlunos().add(escolasDTO.alunosEntity()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Escola não encontrada");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<?> removeAluno(EscolasDTO escolasDTO) {
        try {
            Optional<EscolasEntity> optEscola = escolasRepository.findByCNPJ(escolasDTO.cnpj());
            if (optEscola.isPresent()){
                EscolasEntity escola = optEscola.get();
                return  ResponseEntity.status(HttpStatus.OK).body("Aluno removido: "+escola.getAlunos().remove(escolasDTO.alunosEntity()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Escola não encontrada");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<?> listarAluno(EscolasDTO escolasDTO) {
        try {
            Optional<EscolasEntity> optEscola = escolasRepository.findByCNPJ(escolasDTO.cnpj());
            if (optEscola.isPresent()){
                EscolasEntity escola = optEscola.get();
                return  ResponseEntity.status(HttpStatus.OK).body("Lista de Alunos: "+escola.getAlunos());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Escola não encontrada");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<?> addProfessor(EscolasDTO escolasDTO) {
        try {
            Optional<EscolasEntity> optEscola = escolasRepository.findByCNPJ(escolasDTO.cnpj());
            if (optEscola.isPresent()){
                EscolasEntity escola = optEscola.get();
                return  ResponseEntity.status(HttpStatus.OK).body("Professor adicionado: "+escola.getProfessores().add(escolasDTO.professoresEntity()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Escola não encontrada");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public ResponseEntity<?> removeProfessor(EscolasDTO escolasDTO) {
        try {
            Optional<EscolasEntity> optEscola = escolasRepository.findByCNPJ(escolasDTO.cnpj());
            if (optEscola.isPresent()){
                EscolasEntity escola = optEscola.get();
                return  ResponseEntity.status(HttpStatus.OK).body("Professor removido: "+escola.getProfessores().remove(escolasDTO.professoresEntity()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Escola não encontrada");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<?> listarProfessor(EscolasDTO escolasDTO) {
        try {
            Optional<EscolasEntity> optEscola = escolasRepository.findByCNPJ(escolasDTO.cnpj());
            if (optEscola.isPresent()){
                EscolasEntity escola = optEscola.get();
                return  ResponseEntity.status(HttpStatus.OK).body("Lista de Professores: "+escola.getProfessores());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Escola não encontrada");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
