# @Transactional 기록

### Transactional 로그 분석
```bash
Found thread-bound EntityManager [SessionImpl(1481140994<open>)] for JPA transaction
```
- 현재 스레드에 이미 바인딩된 `EntityManager`가 있다는 의미입니다.
- 이는 Spring이 관리하는 `EntityManager`이며, 이미 스레드 로컬에 존재하고 있어 재사용됩니다.
- 보통 `OpenEntityManagerInViewFilter` 혹은 `OpenEntityManagerInViewInterceptor`에 의해 바인딩된 것일 가능성이 큽니다.

```bash
Creating new transaction with name [com.example.demo.service.UserService.join]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
```
- `@Transactional`이 선언된 `UserService.join()` 메서드가 호출되면서 새로운 트랜잭션이 시작됩니다.
- `PROPAGATION_REQUIRED`: 기존 트랜잭션이 있다면 참여, 없다면 새로 생성.
- `ISOLATION_DEFAULT`: 기본 격리 수준 사용 (DB 설정에 따라 다름 / DB isolation로 지정됨).

```bash
Exposing JPA transaction as JDBC ...
```
- 내부적으로 JPA 트랜잭션이 JDBC 연결로도 노출됩니다.
- Hibernate는 JDBC 레벨에서 실제 DB와 통신하기 때문에 이를 위해 JDBC connection handle이 생성됨.

```bash
Getting transaction for [com.example.demo.service.UserService.join]
```
- AOP 방식으로 트랜잭션을 감싼 `TransactionInterceptor`가 실제 메서드를 호출하기 전 트랜잭션을 획득합니다.

```bash
Found thread-bound EntityManager ...
```
- `UserRepository.save()` 호출 시에도 같은 `EntityManager`를 사용함.
- 같은 트랜잭션 컨텍스트 내이므로 같은 `EntityManager` 재사용.

```bash
Participating in existing transaction
```
- `UserRepository.save()`는 이미 존재하는 트랜잭션에 참여합니다.
- 새로운 트랜잭션을 만들지 않고 상위 트랜잭션(`join()`)에 포함됩니다.

```bash
Getting transaction for [SimpleJpaRepository.save]
```
- `save()` 호출 시 `TransactionInterceptor`가 트랜잭션 정보를 다시 확인합니다.
- 상위 트랜잭션에 참여 중이므로 별도 트랜잭션 생성은 안 함.

```bash
Completing transaction for [SimpleJpaRepository.save]
```
- `save()` 메서드 실행 완료.
- 트랜잭션 처리는 상위 메서드(`join`)에 의해 관리되므로 여기서는 그냥 완료 표시만 함.

```bash
Completing transaction for [UserService.join]
```
- `join()` 메서드 실행이 끝나고, 트랜잭션을 종료하려는 단계입니다.

```bash
Initiating transaction commit
```
- 트랜잭션 커밋 시작.
- 여기서 실제 DB에 반영되는 단계.

```bash
Committing JPA transaction on EntityManager ...
```
- JPA 트랜잭션 커밋 실행.
- 영속성 컨텍스트에 있는 변경 내용이 플러시(flush)되고 DB에 커밋됨.

```bash
Not closing pre-bound JPA EntityManager after transaction
```
- 트랜잭션은 끝났지만 EntityManager는 닫지 않음.
- OpenEntityManagerInViewInterceptor가 바인딩한 것이므로 요청이 완전히 끝날 때까지 유지됨.

```bash
Closing JPA EntityManager in OpenEntityManagerInViewInterceptor
```
- HTTP 요청 응답 완료 직전에 `EntityManager`를 정리합니다.
- View 렌더링 후 또는 JSON 응답 후 정리되는 시점입니다.

### @Transactional 사용하지 않았을 때 로그
```bash
2025-09-17T11:37:50.925+09:00 DEBUG 32411 --- [demo] [nio-8080-exec-3] o.j.s.OpenEntityManagerInViewInterceptor : Opening JPA EntityManager in OpenEntityManagerInViewInterceptor
2025-09-17T11:37:51.435+09:00  INFO 32411 --- [demo] [nio-8080-exec-3] c.e.demo.controller.UserController       : UserController.join requestJoin: RequestJoin[name=a, email=a@a.com]
2025-09-17T11:37:51.437+09:00  INFO 32411 --- [demo] [nio-8080-exec-3] com.example.demo.service.UserService     : UserService.join requestJoin: RequestJoin[name=a, email=a@a.com]
2025-09-17T11:37:51.446+09:00 DEBUG 32411 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Found thread-bound EntityManager [SessionImpl(17429454<open>)] for JPA transaction
2025-09-17T11:37:51.447+09:00 DEBUG 32411 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2025-09-17T11:37:51.451+09:00 DEBUG 32411 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@20cc44ad]
2025-09-17T11:37:51.452+09:00 TRACE 32411 --- [demo] [nio-8080-exec-3] o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
2025-09-17T11:37:51.534+09:00 TRACE 32411 --- [demo] [nio-8080-exec-3] o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
2025-09-17T11:37:51.535+09:00 DEBUG 32411 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
2025-09-17T11:37:51.540+09:00 DEBUG 32411 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(17429454<open>)]
2025-09-17T11:37:51.557+09:00 DEBUG 32411 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Not closing pre-bound JPA EntityManager after transaction
2025-09-17T11:37:51.558+09:00  INFO 32411 --- [demo] [nio-8080-exec-3] com.example.demo.service.AuditService    : AuditService.logUserCreation user: com.example.demo.domain.User@35a87f2d
2025-09-17T11:37:51.562+09:00 DEBUG 32411 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Found thread-bound EntityManager [SessionImpl(17429454<open>)] for JPA transaction
2025-09-17T11:37:51.563+09:00 DEBUG 32411 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2025-09-17T11:37:51.563+09:00 DEBUG 32411 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@712fd6a0]
2025-09-17T11:37:51.563+09:00 TRACE 32411 --- [demo] [nio-8080-exec-3] o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
2025-09-17T11:37:51.564+09:00 TRACE 32411 --- [demo] [nio-8080-exec-3] o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
2025-09-17T11:37:51.565+09:00 DEBUG 32411 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
2025-09-17T11:37:51.568+09:00 DEBUG 32411 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(17429454<open>)]
2025-09-17T11:37:51.569+09:00 DEBUG 32411 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Not closing pre-bound JPA EntityManager after transaction
2025-09-17T11:37:51.648+09:00 DEBUG 32411 --- [demo] [nio-8080-exec-3] o.j.s.OpenEntityManagerInViewInterceptor : Closing JPA EntityManager in OpenEntityManagerInViewInterceptor
```

### @Transactional(propagation = Propagation.REQUIRES_NEW) 로그
```bash
2025-09-17T11:31:41.128+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.j.s.OpenEntityManagerInViewInterceptor : Opening JPA EntityManager in OpenEntityManagerInViewInterceptor
2025-09-17T11:31:41.318+09:00  INFO 26279 --- [demo] [nio-8080-exec-3] c.e.demo.controller.UserController       : UserController.join requestJoin: RequestJoin[name=a, email=a@a.com]
2025-09-17T11:31:41.324+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Found thread-bound EntityManager [SessionImpl(1286678171<open>)] for JPA transaction
2025-09-17T11:31:41.325+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [com.example.demo.service.UserService.join]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2025-09-17T11:31:41.329+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@77503e07]
2025-09-17T11:31:41.329+09:00 TRACE 26279 --- [demo] [nio-8080-exec-3] o.s.t.i.TransactionInterceptor           : Getting transaction for [com.example.demo.service.UserService.join]
2025-09-17T11:31:41.330+09:00  INFO 26279 --- [demo] [nio-8080-exec-3] com.example.demo.service.UserService     : UserService.join requestJoin: RequestJoin[name=a, email=a@a.com]
2025-09-17T11:31:41.332+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Found thread-bound EntityManager [SessionImpl(1286678171<open>)] for JPA transaction
2025-09-17T11:31:41.333+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Participating in existing transaction
2025-09-17T11:31:41.333+09:00 TRACE 26279 --- [demo] [nio-8080-exec-3] o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
2025-09-17T11:31:41.394+09:00 TRACE 26279 --- [demo] [nio-8080-exec-3] o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
2025-09-17T11:31:41.394+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Found thread-bound EntityManager [SessionImpl(1286678171<open>)] for JPA transaction
2025-09-17T11:31:41.395+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Suspending current transaction, creating new transaction with name [com.example.demo.service.AuditService.logUserCreation]
2025-09-17T11:31:41.399+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(2128098990<open>)] for JPA transaction
2025-09-17T11:31:41.399+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@62d4beec]
2025-09-17T11:31:41.399+09:00 TRACE 26279 --- [demo] [nio-8080-exec-3] o.s.t.i.TransactionInterceptor           : Getting transaction for [com.example.demo.service.AuditService.logUserCreation]
2025-09-17T11:31:41.399+09:00  INFO 26279 --- [demo] [nio-8080-exec-3] com.example.demo.service.AuditService    : AuditService.logUserCreation user: com.example.demo.domain.User@50bc138a
2025-09-17T11:31:41.400+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Found thread-bound EntityManager [SessionImpl(2128098990<open>)] for JPA transaction
2025-09-17T11:31:41.400+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Participating in existing transaction
2025-09-17T11:31:41.400+09:00 TRACE 26279 --- [demo] [nio-8080-exec-3] o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
2025-09-17T11:31:41.412+09:00 TRACE 26279 --- [demo] [nio-8080-exec-3] o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
2025-09-17T11:31:41.422+09:00 TRACE 26279 --- [demo] [nio-8080-exec-3] o.s.t.i.TransactionInterceptor           : Completing transaction for [com.example.demo.service.AuditService.logUserCreation]
2025-09-17T11:31:41.423+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
2025-09-17T11:31:41.423+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(2128098990<open>)]
2025-09-17T11:31:41.462+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(2128098990<open>)] after transaction
2025-09-17T11:31:41.463+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Resuming suspended transaction after completion of inner transaction
2025-09-17T11:31:41.463+09:00 TRACE 26279 --- [demo] [nio-8080-exec-3] o.s.t.i.TransactionInterceptor           : Completing transaction for [com.example.demo.service.UserService.join]
2025-09-17T11:31:41.463+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
2025-09-17T11:31:41.463+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(1286678171<open>)]
2025-09-17T11:31:41.464+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Not closing pre-bound JPA EntityManager after transaction
2025-09-17T11:31:41.503+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.j.s.OpenEntityManagerInViewInterceptor : Closing JPA EntityManager in OpenEntityManagerInViewInterceptor
```

#### `@Transactional(propagation = Propagation.REQUIRES_NEW)` 로그 분석
```bash
2025-09-17T11:31:41.395+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Suspending current transaction, creating new transaction with name [com.example.demo.service.AuditService.logUserCreation]
```
- 현재 트랜잭션을 일시 중지하고, 새로운 트랜잭션을 생성하고 있음, 해당 트랜잭션은 `logUserCreation`이라는 메서드에서 생성된 것

```bash
2025-09-17T11:31:41.463+09:00 DEBUG 26279 --- [demo] [nio-8080-exec-3] o.s.orm.jpa.JpaTransactionManager        : Resuming suspended transaction after completion of inner transaction
```
- 내부 트랜잭션이 완료된 후 중단했던 외부 트랜잭션을 다시 시작