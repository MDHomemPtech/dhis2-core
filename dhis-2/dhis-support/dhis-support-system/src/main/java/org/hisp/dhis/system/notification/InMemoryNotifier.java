package org.hisp.dhis.system.notification;

/*
 * Copyright (c) 2004-2017, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.system.collection.JobLocalList;
import org.hisp.dhis.system.collection.JobLocalMap;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Lars Helge Overland
 */
public class InMemoryNotifier
    implements Notifier
{
    private static final Log log = LogFactory.getLog( InMemoryNotifier.class );
    
    private static final int MAX_SIZE = 75;
    
    private JobLocalList<Notification> notifications;
    
    private JobLocalMap<JobType, Object> jobSummaries;
    
    @PostConstruct
    public void init()
    {
        notifications = new JobLocalList<>();
        jobSummaries = new JobLocalMap<>();
    }

    // -------------------------------------------------------------------------
    // Notifier implementation
    // -------------------------------------------------------------------------

    @Override
    public Notifier notify( JobConfiguration id, String message )
    {
        return notify( id, NotificationLevel.INFO, message, false );
    }
    
    @Override
    public Notifier notify( JobConfiguration id, NotificationLevel level, String message )
    {
        return notify( id, level, message, false );
    }

    @Override
    public Notifier notify( JobConfiguration id, NotificationLevel level, String message, boolean completed )
    {
        if ( id != null && !( level != null && level.isOff() ) )
        {
            Notification notification = new Notification( level, id.getJobType(), new Date(), message, completed );

            notifications.get( id ).add( 0, notification );

            if ( notifications.get( id ).size() > MAX_SIZE )
            {
                notifications.get( id ).remove( MAX_SIZE );
            }

            log.info( notification );
        }

        return this;
    }

    @Override
    public Notifier update( JobConfiguration id, String message )
    {
        return update( id, NotificationLevel.INFO, message, false );
    }

    @Override
    public Notifier update( JobConfiguration id, NotificationLevel level, String message )
    {
        return update( id, level, message, false );
    }

    @Override
    public Notifier update( JobConfiguration id, NotificationLevel level, String message, boolean completed )
    {
        if ( id != null && !( level != null && level.isOff() ) )
        {
            if ( notifications.get( id ).size() > 0 )
            {
                notifications.get( id ).remove( notifications.get( id ).size() - 1 );
            }

            notify( id, level, message, completed );
        }

        return this;
    }

    @Override
    public List<Notification> getNotifications( JobConfiguration id, String lastUid )
    {
        List<Notification> list = new ArrayList<>();
        
        if ( id != null )
        {
            for ( Notification notification : notifications.get( id ) )
            {
                if ( lastUid != null && lastUid.equals( notification.getUid() ) )
                {
                    break;
                }
                
                list.add( notification );
            }
        }
        
        return list;
    }

    @Override
    public List<Notification> getNotificationsByJobConfigurationUid( String uid )
    {
        for (Map.Entry<JobConfiguration, List<Notification>> entry : notifications.getInternalMap().entrySet()) {
            JobConfiguration key = entry.getKey();
            List<Notification> notificationList = entry.getValue();

            if ( key.getUid().equals( uid ) )
            {
                return notificationList;
            }
        }

        return null;
    }
    
    @Override
    public Notifier clear( JobConfiguration id )
    {
        if ( id != null )
        {
            notifications.clear( id );
            jobSummaries.get( id ).remove( id.getJobType() );
        }
        
        return this;
    }
    
    @Override
    public Notifier addJobSummary( JobConfiguration id, Object jobSummary )
    {
        return addJobSummary( id, NotificationLevel.INFO, jobSummary );
    }
    
    @Override
    public Notifier addJobSummary( JobConfiguration id, NotificationLevel level, Object jobSummary )
    {
        if ( id != null && !( level != null && level.isOff() ) )
        {
            jobSummaries.get( id ).put( id.getJobType(), jobSummary );
        }
        
        return this;
    }

    @Override
    public Object getJobSummary( JobConfiguration id )
    {
        if ( id != null )
        {
            return jobSummaries.get( id ).get( id.getJobType() );
        }
        
        return null;
    }
}
