var grid = 4;
var item_height = 48;

$(document).ready(function() {
    $("select").on("mousedown", function(e) {
        var _this = this,
            options = $(_this).find("option"),
            wrapper = $('<div class="selection"></div>');

        $(".selection").remove();

        $(_this).focus();

        for (var i = 0; i < options.length; i++) {
            var link = $(
                '<a data-id="' + options[i].value + '">' + options[i].innerText + "</a>"
            );

            wrapper.append(link);
        }

        wrapper.on("click", function(e) {
            $(this).remove();
            $(_this)
                .find("option[value=" + e.target.dataset.id + "]")
                .attr("selected", "selected");
        });

        var top_start = $(_this).position().top - $(_this).height() * 2,
            left_start = $(_this).position().left + $(_this).width();

        var top_finish =
            $(_this).position().top -
            grid * 3 -
            item_height * $(_this).children("option:selected").index(),
            left_finish = $(_this).position().left - grid;

        $(_this).after(wrapper);

        wrapper.css({ top: top_start, left: left_start, height: 0, width: 0 });
        wrapper.animate(
            {
                width: $(_this).outerWidth(),
                height: "auto",
                top: top_finish,
                left: left_finish,
                opacity: 1
            },
            280,
            "easeInOutQuint"
        );

        e.preventDefault();
        e.stopPropagation();
    });
});
