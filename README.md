# Meta

## 메타버스 구현중

### 📌 01/26
#### ✔ 메인 페이지 대략적인 구현
#### ✔ 로그인 및 회원가입 간단하게 연동
#### ✔ 메타 방 생성 및 입장 구현
#### ✔ 메타 방 내부 캐릭터 이동 구현
#### ✔ 웹소켓(STOMP)을 이용한 각 메타 방 내부 채팅 구현
현재까지 대강 구현해둔 로그인과 소켓부분을 메인 페이지를 하나 만들어서 연동하고 본격적으로 메타쪽을 구현해보도록 할것이다.

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
		metaPersonnel INT NOT NULL, #방 모집인원
		metaRecruitingPersonnel INT NOT NULL #방 참여중인 인원
	);

#

### 📌 01/27
#### ✔ 메타 방 내부 참가자 표시 구현
##### 로그인한 유저가 입장하면 현재 참가하고있는 유저들을 참가자란에 이름 및 프로필 사진을 작성하여 보여주도록 만들었다.
##### 하지만 이미 입장해있는 유저한테는 새로고침 하기 전까지는 새로 들어온 유저가 참가자란에 보이지 않는 문제가 있었다.
##### 그래서 이 문제를 해결하고자 웹소켓(STOMP)을 이용하여 입장메세지를 가져올때 참가자도 같이 가져오도록 하여 실시간으로 입장한 유저들이 참가자란에 바로바로 작성되도록 만들었다.
##### 현재 테스트 해본 결과 실시간으로 참가자 닉네임과 사진 이름까진 잘 나오고, 다음에는 사진 이름이 아닌 사진이 나오게 만들어 보도록 하겠다.
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
		metaPersonnel INT NOT NULL, #방 모집인원
		metaRecruitingPersonnel INT NOT NULL #방 참여중인 인원
	);

	메타버스 방 내부
	CREATE TABLE MetaRoom(
		metaNickname VARCHAR(20) PRIMARY KEY, #방 참가자 닉네임
		metaProfileImage VARCHAR(100), #방 참가자 프로필 사진
		metaIdx INT NOT NULL, #방 번호
		CONSTRAINT fk_metaIdx FOREIGN KEY(metaIdx) REFERENCES Meta(metaIdx) ON DELETE CASCADE ON UPDATE CASCADE #포린키 연결
	);

#
	
### 📌 01/29
#### ✔ 메타 방 내부 참가자 표시 구현 2
##### 참가지란에 이제 프로필 사진도 같이 가져와서 프로필 사진과 닉네임이 같이 보일 수 있도록 만들었다.
##### 그리고 웹소켓(STOMP)을 사용하여 퇴장하면 참가자란에 있는 본인도 같이 실시간으로 삭제되게 만들었다.

#

### 📌 01/30
#### ✔ DTO 생성 및 변경
##### Entity를 사용하여 테스트한 코드들을 각 사용처에 맞게 DTO를 만들어 바꿔주었다.
#### ✔ 중복접속 차단
##### 방 접속 후 새로고침시 중복접속으로 방에 참가자 수가 계속 올라가 실질적인 참가자는 한명인데 방에 참여중인 인원은 꽉차는 문제가 발생했다.
##### 그래서 이 문제를 해결하고자 해당 참가자가 새로 방에 들어온 참가자인지 이미 방에 참여중인 참가자인지를 확인하는 코드를 넣어주었다.
##### 그러면 이제 방에 접속 후 새로고침을 아무리 해도 더이상 참여중인 인원은 증가하지 않는다.

#

### 📌 01/31
#### ✔ 참가 및 퇴장에 따른 참여중인 인원 실시간 변경 및 작성
##### 메타 방 내부 좌측 상단에는 방 정보가 입력되는데 여기에 참여중인 인원은 해당 방에 유저가 참가하고 퇴장하는것에 따른 숫자가 변해야한다.
##### 하지만 이는 유저가 처음 들어올때만 작성될뿐 다시 새로고침 하기 전까지는 처음 들어온 그대로 남아있는 문제가 있었다.
##### 그래서 이 문제를 해결하기 위해 유저가 참가하거나 퇴장할때마다 변경되는 참여중인 인원을 웹소켓(STOMP)에 전달하고 다시 전달받아 실시간으로 참여중인 인원을 변경된 인원으로 수정 작성되도록 만들었다.

#

### 📌 02/01
#### ✔ 검색 종류 및 방 분야에 따라 각기 다른 검색 구현
##### 내가 원하는 메타 방을 찾고싶을 경우 사용하는 방 검색기능을 구현했다.
##### 여기서는 두가지 구분점이 존재하는데 바로 검색 종류와 방 분야다.
##### 검색 종류는 방을 번호로 검색할지 이름으로 검색할지를 결정하고, 방 분야는 어떤 방을 검색할지를 결정한다.
##### 그리고 검색 종류는 종류에 따라 검색어가 다르게 들어가야 하기에 DTO에서 if문을 통해 검색 타입을 각각 다르게 지정되게 만들었다.

#
