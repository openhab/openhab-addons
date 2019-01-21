$(function() {

    var body = document.body, mask = document.createElement("div"), activeNav;

    mask.className = "mask";

    /* slide menu left */
    $('.open-menu').click(function(e) {
        e.stopImmediatePropagation();
        e.preventDefault();
        $('body').addClass('sml-open');
        document.body.appendChild(mask);

        /* hide active menu if mask is clicked */
        $(mask).click(function(e) {
            e.stopImmediatePropagation();
            e.preventDefault();
            $('body').removeClass('sml-open');
            document.body.removeChild(mask);
        });
    });

    /* hide active menu if close menu button is clicked */
    [].slice.call($('.close-menu')).forEach(function(el, i) {
        $(el).click(function() {
            $('body').removeClass('sml-open');
            document.body.removeChild(mask);
        });
    });

});