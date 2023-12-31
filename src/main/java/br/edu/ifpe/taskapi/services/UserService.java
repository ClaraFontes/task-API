package br.edu.ifpe.taskapi.services;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import br.edu.ifpe.taskapi.dto.create.UsersDTO;
import br.edu.ifpe.taskapi.dto.read.UserMinDTO;
import br.edu.ifpe.taskapi.dto.read.UsersLoginDTO;
import br.edu.ifpe.taskapi.dto.update.UserUpdateDTO;
import br.edu.ifpe.taskapi.entities.User;
import br.edu.ifpe.taskapi.repositories.ITaskRepository;
import br.edu.ifpe.taskapi.repositories.IUserRepository;


@Service
public class UserService {
	
	@Autowired
	private IUserRepository repository;
	@Autowired
	private ITaskRepository taskRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	

	@Transactional
	public ResponseEntity<?> createUser (@RequestBody UsersDTO userDTO){
		try {
			Optional<User> isCreated = repository.findByEmail(userDTO.getEmail());
			if(isCreated.isPresent()) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já cadastrado!");
			}else {
				String encryptedPassword = passwordEncoder.encode(userDTO.getPassword());
				User newUser = new User(userDTO.getName(),userDTO.getEmail(),encryptedPassword);
				repository.save(newUser);
				return ResponseEntity.status(HttpStatus.CREATED).body("Usuário criado com Sucesso!");
			}
		}catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do Servidor");
		}
		
	}
	
	public ResponseEntity<?> loginUser(@RequestBody UsersLoginDTO userLoginDTO) {	
		try {
			String email = userLoginDTO.getEmail();
			String password = userLoginDTO.getPassword();
			Optional<User> userToLogin = repository.findByEmail(email);
			if (userToLogin.isPresent()) {
			    if (passwordEncoder.matches(password, userToLogin.get().getPassword())) {
						UserMinDTO userMinDTO = new UserMinDTO(userToLogin.get());
						return ResponseEntity.status(HttpStatus.OK).body(userMinDTO);
				}else{
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou Senha incorretos!");
					}
			}else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário Não encontrado");
			}
			
		}catch (Exception e){
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do Servidor");
		}
		
	}
	
	@Transactional
	public ResponseEntity<?> deleteUser(@PathVariable Integer id){
		try {
			taskRepository.deleteTasksByUserId(id);
			repository.deleteById(id);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Usuário deletado com sucesso!");
		}catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do Servidor");
		}
	}

	public ResponseEntity<?> getUserById(@PathVariable Integer id){
		try{
			User userToGet = repository.findById(id).get();
			if (userToGet != null){
				UserMinDTO userMinDto = new UserMinDTO(userToGet);
				return ResponseEntity.status(HttpStatus.OK).body(userMinDto);
			}else{
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário Não encontrado");
			}
		}catch(Exception e){
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do Servidor");
		}
	}

	@Transactional
	public ResponseEntity<?> updateUser(@PathVariable Integer id,@RequestBody UserUpdateDTO userUpdateDto){
		try{
			User user = repository.findById(id).get();
			if (user != null){
				String encryptedPassword = passwordEncoder.encode(userUpdateDto.getPassword());
				user.setPassword(encryptedPassword);
				repository.save(user);
				UserMinDTO userResponse = new UserMinDTO(user);
				return ResponseEntity.status(HttpStatus.OK).body(userResponse);
			}else{
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
			}
		}catch(Exception e){
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do Servidor");
		}
	}
}
