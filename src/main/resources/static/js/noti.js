const icon = document.querySelector('.icon');
const notiDrop = document.querySelector('.notiDrop');
let status = 0; //0이면 클릭 안된 상태, 1이면 클릭 된 상태

function menuOpen(e){
    if ( e == 1 ){
        notiDrop.style.display = "block";
    } else {
        notiDrop.style.display = "none";
    }
}
icon.addEventListener('click', function () {
    if( status == 0 ){
        status = 1;
        menuOpen(status);
    } else{
        status = 0;
        menuOpen(status);
    }
});