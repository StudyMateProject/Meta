# Meta

## 메타버스 구현중

### 📌 01/26
#### ✔ 메인 페이지 대략적인 구현
#### ✔ 로그인 및 회원가입 간단하게 연동
#### ✔ 메타 방 생성 및 입장 구현
#### ✔ 메타 방 내부 캐릭터 이동 구현
#### ✔ 웹소켓(STOMP)을 이용한 각 메타 방 내부 채팅 구현

##### 사용된 데이터베이스 : MySQL - soju
	CREATE DATABASE soju;
	USE soju;

##### 사용된 테이블 : Member, MetaRoom
	멤버 테이블
	CREATE TABLE Member (
		emailId VARCHAR(50) PRIMARY KEY, #이메일 형식 아이디
		pwd VARCHAR(255) NOT NULL, #비밀번호
		name VARCHAR(10) NOT NULL, #이름
		nickname VARCHAR(20) UNIQUE NOT NULL, #닉네임
		birthday DATE NOT NULL, #생년월일
		gender VARCHAR(1) NOT NULL, #성별
		phoneNumber VARCHAR(15) UNIQUE NOT NULL, #핸드폰 번호
		address VARCHAR(100) NOT NULL, #주소
		studyType VARCHAR(10) NOT NULL, #관심있는 분야
		platform VARCHAR(10) NOT NULL, #플랫폼
		roleName VARCHAR(100) NOT NULL #Spring Security 권한
	);
	
	메타버스 방
	CREATE TABLE MetaRoom (
		metaIdx INT PRIMARY KEY AUTO_INCREMENT, #방 번호
		metaTitle VARCHAR(50) NOT NULL, #방 제목
		metaType VARCHAR(10) NOT NULL, #방 분야
		metaPersonnel INT NOT NULL, #방 모집 인원
		metaRecruitingPersonnel INT NOT NULL #방 모집된 인원
	);

### 📌 01/27
#### ✔ 메타 방 내부 참가자 표시 구현
##### 로그인한 유저가 입장하면 현재 참가하고있는 유저들을 참가자란에 이름 및 프로필 사진을 작성하여 보여주도록 만들었다.
##### 하지만 이미 입장해있는 유저한테는 새로고침 하기 전까지는 새로 들어온 유저가 참가자란에 보이지 않는 문제가 있었다.
##### 그래서 이 문제를 해결하고자 웹소켓(STOMP)을 이용하여 입장메세지를 가져올때 참가자도 같이 가져오도록 하여 실시간으로 입장한 유저들이 참가자란에 바로바로 작성되도록 만들었다.
#### ✔ 테이블 변경
##### 메타 방 내부에 참가자를 작성하기 위해 테이블을 하나 새로 만드는데 이 테이블 이름을 MetaRoom으로 하는것이 적절해보여 MetaRoom테이블을 Meta로 바꿔준뒤, MetaRoom테이블을 새로 만들었다.
##### 그리고 Member테이블에 프로필 사진을 추가해주었다.

##### 사용된 데이터베이스 : MySQL - soju
	CREATE DATABASE soju;
	USE soju;

##### 사용된 테이블 : Member, MetaRoom
	멤버 테이블
	CREATE TABLE Member (
		##################회원가입 전 입력##################
		emailId VARCHAR(50) PRIMARY KEY, #이메일 형식 아이디
		pwd VARCHAR(255) NOT NULL, #비밀번호
		name VARCHAR(10) NOT NULL, #이름
		nickname VARCHAR(20) UNIQUE NOT NULL, #닉네임
		birthday DATE NOT NULL, #생년월일
		gender VARCHAR(1) NOT NULL, #성별
		phoneNumber VARCHAR(15) UNIQUE NOT NULL, #핸드폰 번호
		address VARCHAR(100) NOT NULL, #주소
		studyType VARCHAR(10) NOT NULL, #관심있는 분야
		platform VARCHAR(10) NOT NULL, #플랫폼
		roleName VARCHAR(100) NOT NULL, #Spring Security 권한	
		##################회원가입 후 입력##################
		profileImage VARCHAR(100) #프로필 사진
	);
	
	메타버스 방
	CREATE TABLE Meta(
		metaIdx INT PRIMARY KEY AUTO_INCREMENT, #방 번호
		metaTitle VARCHAR(50) NOT NULL, #방 제목
		metaType VARCHAR(10) NOT NULL, #방 분야
		metaPersonnel INT NOT NULL, #방 모집 인원
		metaRecruitingPersonnel INT NOT NULL #방 모집된 인원
	);

	메타버스 방 내부
	CREATE TABLE MetaRoom(
		metaNickname VARCHAR(20) PRIMARY KEY, #방 참가자 닉네임
		metaProfileImage VARCHAR(100), #방 참가자 프로필 사진
		metaIdx INT NOT NULL, #방 번호
		CONSTRAINT fk_metaIdx FOREIGN KEY(metaIdx) REFERENCES Meta(metaIdx) ON DELETE CASCADE ON UPDATE CASCADE #포린키 연결
	);
