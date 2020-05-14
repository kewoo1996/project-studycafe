"# project-studycafe" 
"# project-studycafe" 

# 스터디 카페 좌석 시스템

본 시스템은 MSA/DDD/Event Storming/EDA 를 포괄하는 분석/설계/구현/운영 전단계를 커버하도록 구성한 시스템입니다.

# Table of contents

- [스터디 카페 시스템](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
  - [신규 개발 조직의 추가](#신규-개발-조직의-추가)

# 서비스 시나리오

기능적 요구사항
1. 고객이 좌석을 선택한다.
1. 좌석을 선택하면 결제가 완료된다.
1. 결제가 완료되면 스터디카페시스템에 등록된다.
1. 좌석을 취소하면 스터디카페시스템에 등록이 취소된다.
1. 고객이 좌석 상태를 중간중간 조회한다.

비기능적 요구사항
1. 트랜잭션
    1. 고객이 예약한 건에 대하여 관리자가 예약가능 여부를 확인한 후에 결제를 진행한다. 
1. 장애격리
    1. 결제 기능이 수행되지 않더라도 좌석 선택은 365일 24시간 받을 수 있어야 한다. Async (event-driven), Eventual Consistency
1. 성능
    1. 고객이 좌석선택 상태를 예약시스템(프론트엔드)에서 확인할 수 있어야 한다. CQRS

# 체크포인트

- 분석 설계


  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?
- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅: 
  - 무정지 운영 CI/CD (10)
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 
    - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?


# 분석/설계

## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과: http://msaez.io/#/storming/5zc7ReYN1OMINiaqqxPWRn09Dty1/mine/c380eb83dc485292b07d43cd41e6f796/-M7CmtNRV2cEg3Vn7Daf



### 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

![image](https://user-images.githubusercontent.com/47409020/81829886-5d2aff80-9576-11ea-9b8c-c72e865fd75c.png)

    - 고객이 좌석을 선택한다. (ok)
    - 좌석을 선택하면 결제가 완료된다. (ok)
    - 결제가 완료되면 스터디카페시스템에 등록된다. (ok)
    - 좌석을 취소하면 스터디카페시스템에 등록이 취소된다. (ok)
    - 고객이 좌석 상태를 중간중간 조회한다. (ok)


### 비기능 요구사항에 대한 검증
![es-08](https://user-images.githubusercontent.com/63624005/81761245-68ddde00-9504-11ea-8a48-35891592d982.jpg)

    - 트랜잭션 (1)
      . 고객이 예약한 건에 대하여 관리자가 예약가능 여부를 확인한 후에 결제를 진행한다. 
    - 장애격리 (2)
      . 결제 기능이 수행되지 않더라도 좌석 선택은 365일 24시간 받을 수 있어야 한다. 
        [Async (event-driven), Eventual Consistency]
    - 성능 (3)
      . 고객이 좌석선택 상태를 예약시스템(프론트엔드)에서 확인할 수 있어야 한다. [CQRS]

## 헥사고날 아키텍처 다이어그램 도출
- 1차
![es-09](https://user-images.githubusercontent.com/63624005/81773899-56739c80-9524-11ea-97dc-cb1b968c2046.jpg)

    - 빠른 고객응답 보다는 서비스의 안정성을 더욱 중시하는 비즈니스적인 이유로 수정하였다. (Pub/Sub)

- 최종
![es-09-1](https://user-images.githubusercontent.com/63624005/81773921-5e334100-9524-11ea-855a-a72b69b77503.jpg)

# 구현

httpie, siege 툴 설치

httpie접속 
kubectl exec -it httpie bin/bash

replicaset 개수 조정
kubectl scale deploy nginx --replicas=3
kubectl scale deploy payment --replicas=3

pub/sub / cqrs
http POST reservation:8080/reservations customerId=1111
http payment:8080/payments
http studycafe:8080/studycafes
http reservation:8080/reservationViews

kubectl get all

토픽 리스트 보기
kubectl -n kafka exec my-kafka-0 -- /usr/bin/kafka-topics --zookeeper my-kafka-zookeeper:2181 --list

이벤트 수신하기
kubectl -n kafka exec -ti my-kafka-0 -- /usr/bin/kafka-console-consumer --bootstrap-server my-kafka:9092 --topic projectStart --from-beginning




분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트와 파이선으로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다. (각자의 포트넘버는 8081 ~ 808n 이다)

```
# studycafe //port number: 8081
cd studycafe
mvn spring-boot:run

# payment //port number: 8082
cd payment
mvn spring-boot:run

# reservation //port number: 8083
cd reservation
mvn spring-boot:run
```

## DDD 의 적용

각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity로 선언하였다(예시는 reservationsystem 마이크로 서비스).  
  이때 가능한 현업에서 사용하는 언어(유비쿼터스 랭귀지)를 그대로 사용하여 모델링시 영문화 하였다.

```
package projectStart;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long customerId;

    @PostPersist
    public void onPostPersist(){
        Reserved reserved = new Reserved();
        BeanUtils.copyProperties(this, reserved);
        reserved.publish();
    }

    @PostRemove
    public void onPostRemove(){
        ReservationCanceled reservationCanceled = new ReservationCanceled();
        BeanUtils.copyProperties(this, reservationCanceled);
        reservationCanceled.publishAfterCommit();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

}
Entity Pattern과 Repository Pattern을 적용하여 JPA를 통하여 다양한 데이터소스 유형(RDB)에 대한 별도의 처리가 없도록, 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST의 RestRepository를 적용하였다.

```
package roomreservation;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface ReservationRepository extends PagingAndSortingRepository<Reservation, Long>{
```


## 적용 후 REST API 의 테스트
- reservation 서비스에서 좌석요청 
```  
  http POST reservation:8080/reservations customerId=1111\
  
```   

![dv-01](https://user-images.githubusercontent.com/63624005/81763734-df7dda00-950a-11ea-9793-34abab44c077.png)


- management 서비스 확인

![dv-02](https://user-images.githubusercontent.com/63624005/81763750-e9074200-950a-11ea-8d9a-f533be2ffbde.png)
  

- managementList 서비스에서 reserveId 저장 확인
```  
http localhost:8082/managementLists/1
``` 

![dv-03](https://user-images.githubusercontent.com/63624005/81763766-f15f7d00-950a-11ea-9ea3-d138ee246485.png)


- management 서비스의 승인처리
```  
http localhost:8082/managements reserveId=”reserve1”
``` 

![dv-04](https://user-images.githubusercontent.com/63624005/81763782-f9b7b800-950a-11ea-94d2-b6c9d96e9c59.png)


- payment 서비스 확인

![dv-05](https://user-images.githubusercontent.com/63624005/81763795-03412000-950b-11ea-8597-a3c0713cd5fd.png)
  

- kafka 수신 확인
 
![dv-06](https://user-images.githubusercontent.com/63624005/81763810-0b995b00-950b-11ea-99fa-13e089a3060b.png)
  


## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

모든 시스템은 장애 상태나 유지보수 상태일 때, 잠시 시스템이 내려가도 작동에 문제가 없게 하기 위해 동기식이 아닌 비동기식으로 처리한다. 그 중 고객의 예약이 이루어진 후에 Management시스템을 이를 알려주는 행위를 예를 들었다.

- reservation 시스템에 고객의 예약을 받았다는 기록을 남긴 후에 예약이 되었다는 도메인 이벤트를 카프카로 송출한다.(Publish)

```
@PostPersist
    public void onPostPersist() {
        Reserved reserved = new Reserved();
        BeanUtils.copyProperties(this, reserved);
        reserved.publish();
    }
```

- management 시스템에서는 예약완료 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:
```
public class PolicyHandler{
...

@StreamListener(KafkaProcessor.INPUT)
    public void wheneverReserved_ReserveConfirm(@Payload Reserved reserved){

        if(reserved.isMe()){
            System.out.println("##### listener ReserveConfirm : " + reserved.toJson());
        }
    }
```

management와 시스템은 reservation, payment 시스템과 완전히 분리되어 있으며, 이벤트 수신에 따라 처리되기 때문에 시스템이 유지보수로 인해 잠시 내려간 예약을 받아 managementList에 저장하는데에 아무 문제 없다.


- management 시스템을 잠시 내려 놓음

![dv-11](https://user-images.githubusercontent.com/63624005/81765091-eeb25700-950d-11ea-92f6-436329052752.png)


- 예약 처리
```
http POST localhost:8081/reservations reserveId=”reserve2” userId=”user1”   #Success
http POST localhost:8081/reservations reserveId=”reserve3” userId=”user1”   #Success
```

- 예약상태 확인
```
http localhost:8081/reservations     
``` 

![dv-12](https://user-images.githubusercontent.com/63624005/81765107-f70a9200-950d-11ea-8e3b-029d9b1bb629.png)


- 예약 완료 상태까지 Event 진행 확인

![dv-13](https://user-images.githubusercontent.com/63624005/81765130-012c9080-950e-11ea-84d3-9d3a4f6136ba.png)


- management 시스템 재기동 후 management 시스템에 Update 되었는지 확인(CQRS)
  고객이 숙소에 예약 신청한 내역을 managementList view에서 확인할 수 있다.

![dv-14](https://user-images.githubusercontent.com/63624005/81765144-0984cb80-950e-11ea-9c98-dd84597c3825.png)

![dv-15](https://user-images.githubusercontent.com/63624005/81765184-1b666e80-950e-11ea-8722-60464240fe71.png)

![dv-16](https://user-images.githubusercontent.com/63624005/81765197-228d7c80-950e-11ea-8ff6-835dbc760c26.png)


## API 게이트웨이

 Clous 환경에서는Clous 환경에서는 //서비스명:8080 에서 Gateway API가 작동해야함 
 application.yml 파일에 profile별 gateway 설정

- Gateway 설정 파일 
 GATEWAY 

![dv-17](https://user-images.githubusercontent.com/63624005/81765217-2caf7b00-950e-11ea-8d8b-935347e5dfc8.png)



# 운영

## CI/CD 설정

각 구현체들은 각자의 source repository 에 구성되었고, pipeline build script 는 각 프로젝트 폴더 이하에 azure-pipelines.yml 에 포함되었다.

*devops를 활용하여 pipeline을 구성하였고, CI CD 자동화를 구현하였다.

![image](https://user-images.githubusercontent.com/63624035/81771610-71431280-951e-11ea-91e9-8498a62e636e.png)

* 아래와 같이 pod 가 정상적으로 올라간 것을 확인하였다.
![image](https://user-images.githubusercontent.com/63624035/81761872-156c8f80-9506-11ea-8c23-55f8d347a2c8.png)

* 아래와 같이 쿠버네티스에 모두 서비스로 등록된 것을 확인할 수 있다.
![image](https://user-images.githubusercontent.com/63624035/81762154-de4aae00-9506-11ea-99b1-1b6068e34547.png)

### 오토스케일 아웃
시스템을 안정되게 운영할 수 있도록 보완책으로 자동화된 확장 기능을 적용하고자 한다. 

- 안정성이 중요한 결제서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다:
```
kubectl autoscale deploy payment --min=1 --max=10 --cpu-percent=15
```
- 워크로드를 동시사용자 10명으로 20초 동안 걸어준다.
![시즈적용_10](https://user-images.githubusercontent.com/63624014/81764751-3edce980-950d-11ea-806e-d8f51a26c46d.PNG)

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다. 동시 사용자 10명으로 한 경우에는 시스템에 변화가 없다.
![시즈적용_10_3](https://user-images.githubusercontent.com/63624014/81764796-574d0400-950d-11ea-88d4-56428f5be633.PNG)

- siege 의 로그를 보명 10명까지는 성능에 문제가 없어보인다. 
![시즈적용_10_2](https://user-images.githubusercontent.com/63624014/81764775-4c926f00-950d-11ea-93b8-ae86f7bb4cf5.PNG)



- 워크로드를 동시사용자 100명으로 20초 동안 걸어준다.
![시즈적용_100_1](https://user-images.githubusercontent.com/63624014/81766034-42be3b00-9510-11ea-8682-dab260440772.PNG)

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다. 시스템이 중간에 멈추는 경우가 발생한다.
![시즈적용_100_2](https://user-images.githubusercontent.com/63624014/81766051-4d78d000-9510-11ea-82c2-6a830718042c.PNG)

- siege 의 로그를 보명 100일 때는 95%정도의 서비스 Available을 유지하고, SLA 수준에 따라 오토스케일 아웃을 지속적으로 조정한다.
![시즈적용_100_3](https://user-images.githubusercontent.com/63624014/81766079-59649200-9510-11ea-96c5-dc233ed007b6.PNG)


## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함

- seige 로 배포작업 직전에 워크로드를 모니터링 함.

![image](https://user-images.githubusercontent.com/63624035/81765032-ccb8d480-950d-11ea-9ca8-ec492af06c01.png)

![image](https://user-images.githubusercontent.com/63624035/81764389-6da69000-950c-11ea-98d6-114141561d3d.png)

- 새버전으로의 배포 시작

![image](https://user-images.githubusercontent.com/63624035/81765398-ac3d4a00-950e-11ea-8e3b-a01e66031559.png)

- seige 의 화면으로 넘어가서 Availability 가 100% 미만으로 떨어졌는지 확인
```
Transactions:		        3078 hits
Availability:		       70.45 %
Elapsed time:		       120 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02

```
배포기간중 Availability 가 평소 100%에서 70% 대로 떨어지는 것을 확인. 원인은 쿠버네티스가 성급하게 새로 올려진 서비스를 READY 상태로 인식하여 서비스 유입을 진행한 것이기 때문. 이를 막기위해 Readiness Probe 를 설정함:

![image](https://user-images.githubusercontent.com/63624035/81766634-78afef00-9511-11ea-8573-23a287118556.png)


- 동일한 시나리오로 재배포 한 후 Availability 확인:
```
Transactions:		        3078 hits
Availability:		       100 %
Elapsed time:		       120 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02

```

배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.
