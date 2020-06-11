## Travis tests were successful
Hey @{{pullRequestAuthor}},
we found no major flaws with your code. Still you might want to look at this logfile, as we usually suggest some optional improvements.

{{#jobs}}
### {{displayName}}
{{#scripts}}
<details>
  <summary>
    <strong>
     {{command}}
    </strong>
  </summary>

```
{{&contents}}
```
</details>
<br />
{{/scripts}}
{{/jobs}}
