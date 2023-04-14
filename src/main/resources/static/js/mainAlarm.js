//알림 x(삭제)버튼 클릭시 실행될 메서드
function deleteAlarm(f) {
    let idx = f.idx.value;
    let url = "/mypage/alarm/delete";
    let param = "idx=" + idx;
    sendRequest(url, param, deleteRes, "GET");
}

function deleteRes() {
    if ( xhr.readyState == 4 && xhr.status == 200 ) {
        var data = xhr.responseText;
        if(data == 'no'){
            alert("삭제 실패");
            location.reload();
        } else {
            alert("삭제 성공");
            location.reload();
        }
    }
}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//알림 수락버튼 클릭시 실행될 메서드
function recruitAccept(f) {
    let idx = f.idx.value;
    let recruitStudyIdx = f.recruitStudyIdx.value;
    let recruitMentorIdx = f.recruitMentorIdx.value;
    let recruitMenteeIdx = f.recruitMenteeIdx.value;
    let nickname = f.nickname.value;
    let url = "/mypage/alarm/accept";
    let param = "idx=" + idx +
                "&recruitStudyIdx=" + recruitStudyIdx +
                "&recruitMentorIdx=" + recruitMentorIdx +
                "&recruitMenteeIdx=" + recruitMenteeIdx +
                "&nickname=" + nickname;
    sendRequest(url, param, acceptRes, "GET");
}

function acceptRes() {
    if ( xhr.readyState == 4 && xhr.status == 200 ) {
        var data = xhr.responseText;
        if(data == "no"){
            alert("실패");
            return;
        } else {
            if(data == 'study') {
                alert("스터디원 구하기 수락 성공");
                location.reload();
            } else if(data == 'mentor') {
                alert("멘토 구하기 수락 성공");
                location.reload();
            } else if(data == 'mentee') {
                alert("멘티 구하기 수락 성공");
                location.reload();
            } else if(data == 'overlap') {
                alert("신청한 사람은 이미 현재 만남 진행중 입니다.");
                return;
            } else if(data == 'excess') {
                alert("인원이 다 구해져 받을 수 없습니다.");
                return;
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//알림 거절버튼을 눌렀을 때 실행될 메서드
function recruitRefuse(f) {
    let idx = f.idx.value;
    let recruitStudyIdx = f.recruitStudyIdx.value;
    let recruitMentorIdx = f.recruitMentorIdx.value;
    let recruitMenteeIdx = f.recruitMenteeIdx.value;
    let url = "/mypage/alarm/refuse"
    let param = "idx=" + idx;
    sendRequest(url, param, refuseRes, "GET");
}

function refuseRes() {
    if ( xhr.readyState == 4 && xhr.status == 200 ) {
        var data = xhr.responseText;
        if(data == 'no'){
            alert("거절 실패");
            location.reload();
        } else {
            alert("거절 성공");
            location.reload();
        }
    }
}